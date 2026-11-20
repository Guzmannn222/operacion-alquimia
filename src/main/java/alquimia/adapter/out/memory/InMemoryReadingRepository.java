package alquimia.adapter.out.memory;

import alquimia.domain.model.Reading;
import alquimia.domain.model.SurveillanceDevice;
import alquimia.domain.port.out.ReadingRepositoryPort;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

/** Output adapter: keeps the information in memory only (no database, as the exam requires). */
@Repository
public class InMemoryReadingRepository implements ReadingRepositoryPort {

	private final Map<SurveillanceDevice, Reading> store = new ConcurrentHashMap<>();

	@Override
	public void save(Reading reading) {
		store.put(reading.device(), reading);
	}

	@Override
	public Optional<Reading> findByDevice(SurveillanceDevice device) {
		return Optional.ofNullable(store.get(device));
	}

	@Override
	public List<Reading> findAll() {
		List<Reading> all = new ArrayList<>(store.values());
		all.sort(Comparator.comparing(Reading::device));
		return all;
	}

	@Override
	public void deleteByDevice(SurveillanceDevice device) {
		store.remove(device);
	}
}
