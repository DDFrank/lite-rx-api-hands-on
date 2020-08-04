package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Learn how to control the demand.
 *
 * @author Sebastien Deleuze
 */
public class Part06RequestTest {
	public static String NEW_LINE = System.getProperty("line.separator");
	Part06Request workshop = new Part06Request();
	ReactiveRepository<User> repository = new ReactiveUserRepository();

	PrintStream originalConsole = System.out;
	ByteArrayOutputStream logConsole;
	String threadInfos = "\\d{2}:\\d{2}:\\d{2}\\.\\d{3}\\s{1}\\[\\S+\\]\\s{1}(INFO)\\s{2}(reactor\\.Flux\\.Zip\\.1)\\s{1}-\\s{1}";

	@BeforeEach
	public void beforeEach() {
		logConsole = new ByteArrayOutputStream();
		System.setOut(new PrintStream(logConsole));
	}

	@AfterEach
	public void afterEach() {
		originalConsole.println(logConsole.toString());
		System.setOut(originalConsole);
	}

//========================================================================================

	@Test
	public void requestAll() {
		Flux<User> flux = repository.findAll();
		StepVerifier verifier = workshop.requestAllExpectFour(flux);
		verifier.verify();
	}

//========================================================================================

	@Test
	public void requestOneByOne() {
		Flux<User> flux = repository.findAll();
		StepVerifier verifier = workshop.requestOneExpectSkylerThenRequestOneExpectJesse(flux);
		verifier.verify();
	}

//========================================================================================

	@Test
	public void experimentWithLog() {
		Flux<User> flux = workshop.fluxWithLog();
		StepVerifier.create(flux, 0)
				.thenRequest(1)
				.expectNextMatches(u -> true)
				.thenRequest(1)
				.expectNextMatches(u -> true)
				.thenRequest(2)
				.expectNextMatches(u -> true)
				.expectNextMatches(u -> true)
				.verifyComplete();

		String log = logConsole.toString().replaceAll(threadInfos, "");

		assertThat(log)
				.contains("onSubscribe(FluxZip.ZipCoordinator)" + NEW_LINE
						+ "request(1)" + NEW_LINE
						+ "onNext(Person{username='swhite', firstname='Skyler', lastname='White'})" + NEW_LINE
						+ "request(1)" + NEW_LINE
						+ "onNext(Person{username='jpinkman', firstname='Jesse', lastname='Pinkman'})" + NEW_LINE
						+ "request(2)" + NEW_LINE
						+ "onNext(Person{username='wwhite', firstname='Walter', lastname='White'})" + NEW_LINE
						+ "onNext(Person{username='sgoodman', firstname='Saul', lastname='Goodman'})" + NEW_LINE
						+ "onComplete()" + NEW_LINE);
	}

//========================================================================================

	@Test
	public void experimentWithDoOn() {
		Flux<User> flux = workshop.fluxWithDoOnPrintln();
		StepVerifier.create(flux)
				.expectNextCount(4)
				.verifyComplete();

		assertThat(logConsole.toString())
				.contains("Starring:" + NEW_LINE
						+ "Skyler White" + NEW_LINE
						+ "Jesse Pinkman" + NEW_LINE
						+ "Walter White" + NEW_LINE
						+ "Saul Goodman" + NEW_LINE
						+ "The end!" + NEW_LINE);
	}

}
