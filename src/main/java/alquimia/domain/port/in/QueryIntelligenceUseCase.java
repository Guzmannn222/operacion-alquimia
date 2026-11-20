package alquimia.domain.port.in;

import alquimia.domain.model.IntelligenceResult;
import alquimia.domain.model.Reading;
import java.util.List;

/** 2.3 Query the accumulated information and compute the position and the message. */
public interface QueryIntelligenceUseCase {

	IntelligenceResult query();

	Reading findDevice(String deviceName);

	List<Reading> listDevices();
}
