package alquimia.domain.port.out;

import alquimia.domain.model.Reading;
import alquimia.domain.model.SurveillanceDevice;
import java.util.List;
import java.util.Optional;

/** Output port: temporary storage of the information received from each device. */
public interface ReadingRepositoryPort {

	void save(Reading reading);

	Optional<Reading> findByDevice(SurveillanceDevice device);

	List<Reading> findAll();

	void deleteByDevice(SurveillanceDevice device);
}
