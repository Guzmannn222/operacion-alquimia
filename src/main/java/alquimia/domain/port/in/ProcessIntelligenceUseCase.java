package alquimia.domain.port.in;

import alquimia.domain.model.IntelligenceResult;
import alquimia.domain.model.Reading;
import java.util.List;

/** 2.1 Process the information of the three devices received at once. */
public interface ProcessIntelligenceUseCase {

	IntelligenceResult process(List<Reading> readings);
}
