package alquimia.domain.port.in;

import alquimia.domain.model.Reading;
import java.util.List;

/** 2.5 Update only the distance and/or the message of one device (null keeps the current value). */
public interface UpdateReadingUseCase {

	Reading update(String deviceName, Double distance, List<String> message);
}
