package alquimia.domain.port.in;

import alquimia.domain.model.Reading;
import java.util.List;

/** 2.2 Register the information of one device. */
public interface RegisterReadingUseCase {

	Reading register(String deviceName, Double distance, List<String> message);
}
