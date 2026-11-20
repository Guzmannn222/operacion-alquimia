package alquimia.domain.port.in;

import alquimia.domain.model.Reading;
import java.util.List;

/** 2.4 Replace all the information of one device. */
public interface ReplaceReadingUseCase {

	Reading replace(String deviceName, Double distance, List<String> message);
}
