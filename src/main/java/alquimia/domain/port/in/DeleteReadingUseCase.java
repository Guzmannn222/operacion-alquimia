package alquimia.domain.port.in;

/** 2.6 Delete the information of one device. */
public interface DeleteReadingUseCase {

	void delete(String deviceName);
}
