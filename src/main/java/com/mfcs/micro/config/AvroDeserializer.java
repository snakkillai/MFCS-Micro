package com.mfcs.micro.config;

import org.apache.avro.Schema;
import org.apache.avro.io.DatumReader;
import org.apache.avro.io.Decoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.specific.SpecificDatumReader;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Kafka {@link Deserializer} for Apache Avro {@link SpecificRecordBase} types.
 * <p>
 * Deserializes binary Avro-encoded messages from Kafka using the specific
 * Avro record class determined by the configured {@code targetType}.
 * Supports an optional 5-byte Confluent Schema Registry wire-format prefix
 * (magic byte + schema-id) for compatibility with Schema Registry producers.
 *
 * @param <T> the Avro specific record type
 */
public class AvroDeserializer<T extends SpecificRecordBase> implements Deserializer<T> {

    private static final Logger log = LoggerFactory.getLogger(AvroDeserializer.class);

    /** Magic byte used by Confluent Schema Registry wire format. */
    private static final byte CONFLUENT_MAGIC_BYTE = 0x0;

    /** Number of bytes in the Confluent Schema Registry wire-format prefix. */
    private static final int CONFLUENT_PREFIX_BYTES = 5;

    private final Class<T> targetType;

    public AvroDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        // No additional configuration needed
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        if (data == null || data.length == 0) {
            return null;
        }
        try {
            Schema writerSchema = getSchema();
            DatumReader<T> datumReader = new SpecificDatumReader<>(writerSchema);

            byte[] avroBytes = stripConfluentPrefix(data);
            Decoder decoder = DecoderFactory.get().binaryDecoder(avroBytes, null);
            T record = datumReader.read(null, decoder);
            log.debug("Deserialized Avro record of type {} from topic {}", targetType.getSimpleName(), topic);
            return record;
        } catch (Exception ex) {
            log.error("Failed to deserialize Avro message from topic {}: {}", topic, ex.getMessage());
            throw new SerializationException(
                    "Error deserializing Avro message for type " + targetType.getName(), ex);
        }
    }

    @Override
    public void close() {
        // Nothing to close
    }

    /**
     * Returns the Avro schema for the target type by instantiating it once.
     */
    private Schema getSchema() throws ReflectiveOperationException {
        return targetType.getDeclaredConstructor().newInstance().getSchema();
    }

    /**
     * Strips the 5-byte Confluent Schema Registry wire-format prefix if present,
     * so that the deserializer works with both Schema-Registry-produced and
     * plain Avro-binary messages.
     */
    private byte[] stripConfluentPrefix(byte[] data) {
        if (data.length > CONFLUENT_PREFIX_BYTES && data[0] == CONFLUENT_MAGIC_BYTE) {
            byte[] stripped = new byte[data.length - CONFLUENT_PREFIX_BYTES];
            System.arraycopy(data, CONFLUENT_PREFIX_BYTES, stripped, 0, stripped.length);
            return stripped;
        }
        return data;
    }
}
