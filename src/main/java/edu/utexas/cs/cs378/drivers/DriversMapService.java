package edu.utexas.cs.cs378.drivers;

import edu.utexas.cs.cs378.Archetypes;
import edu.utexas.cs.cs378.BinarySerializer;
import lombok.SneakyThrows;

public class DriversMapService {
    @SneakyThrows
    public static void main(String[] args) {
        // 20 bytes per record: 16 byte driver hash + short duration + short earnings

        // original: parse the CSV and stream it straight to the reduction
        Archetypes.mappingService(20,
                DriverEarnRateData::new,
                BinarySerializer::convertToByteArray,
                data -> data.getTotal() < 30000 && data.getTip() < 30000 && data.getTip() > 500 && data.getTotal() > 500
        );

        // split: compute the map output into <dataset>.drivers, then stream the file
//        Archetypes.fileMappingService(20,
//                ".drivers",
//                DriverEarnRateData::new,
//                BinarySerializer::convertToByteArray,
//                data -> data.getTotal() < 30000 && data.getTip() < 30000 && data.getTip() > 500 && data.getTotal() > 500
//        );

        // read the parsed data from file and then send that to the reduction servers.
//        Archetypes.fromFileMappingService(".drivers");
    }
}
