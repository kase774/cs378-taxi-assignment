package edu.utexas.cs.cs378.drivers;

import edu.utexas.cs.cs378.Archetypes;
import edu.utexas.cs.cs378.BinarySerializer;
import lombok.SneakyThrows;

public class DriversMapService {
    @SneakyThrows
    public static void main(String[] args) {
        // 20 bytes per record: 16 byte driver hash + short duration + short earnings
        Archetypes.mappingService(20,
                DriverEarnRateData::new,
                BinarySerializer::convertToByteArray,
                data -> data.getTotal() < 30000 && data.getTip() < 30000 && data.getTip() > 500 && data.getTotal() > 500
        );
    }
}
