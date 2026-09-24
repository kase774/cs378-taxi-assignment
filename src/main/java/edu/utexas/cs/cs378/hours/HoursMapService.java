package edu.utexas.cs.cs378.hours;

import edu.utexas.cs.cs378.Archetypes;
import edu.utexas.cs.cs378.BinarySerializer;
import lombok.SneakyThrows;

public class HoursMapService {
    @SneakyThrows
    public static void main(String[] args) {
        Archetypes.mappingService(3,
                TripHourData::new,
                BinarySerializer::convertToByteArray,
                data -> data.getTotal() < 30000 && data.getTip() < 30000 && data.getTip() > 500 && data.getTotal() > 500
        );
    }

}
