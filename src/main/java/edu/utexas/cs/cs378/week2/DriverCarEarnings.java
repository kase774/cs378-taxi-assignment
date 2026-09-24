package edu.utexas.cs.cs378.week2;

import edu.utexas.cs.cs378.Md5Wrapper;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DriverCarEarnings {

    @EqualsAndHashCode.Include
    final Md5Wrapper driver;
    final Set<Md5Wrapper> cars = new HashSet<>();
    int totalEarnings = 0;

    public DriverCarEarnings(Md5Wrapper driver) {
        this.driver = driver;
    }

    public void registerNewData(TripDriverCarTotalData data) {
        this.cars.add(new Md5Wrapper(data.getCarHash()));
        this.totalEarnings += data.getTotal();
    }
}
