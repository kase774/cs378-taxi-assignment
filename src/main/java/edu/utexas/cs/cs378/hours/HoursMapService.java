package edu.utexas.cs.cs378.hours;

import edu.utexas.cs.cs378.Archetypes;
import edu.utexas.cs.cs378.BinarySerializer;
import lombok.SneakyThrows;

public class HoursMapService {
    @SneakyThrows
    public static void main(String[] args) {
        // original: parse the CSV and stream it straight to the reduction
        Archetypes.mappingService(3,
                TripHourData::new,
                BinarySerializer::convertToByteArray,
                data -> data.getTotal() < 30000 && data.getTip() < 30000 && data.getTip() > 500 && data.getTotal() > 500
        );

        // split: compute the map output into <dataset>.hours, then stream the file
//        Archetypes.fileMappingService(3,
//                ".hours",
//                TripHourData::new,
//                BinarySerializer::convertToByteArray,
//                data -> data.getTotal() < 30000 && data.getTip() < 30000 && data.getTip() > 500 && data.getTotal() > 500
//        );

        // read the parsed data from file and then send that to the reduction servers.
//        Archetypes.fromFileMappingService(".hours");
    }

}
