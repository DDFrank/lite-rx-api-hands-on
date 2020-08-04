package io.pivotal.literx;

import io.pivotal.literx.domain.User;
import io.pivotal.literx.repository.ReactiveRepository;
import io.pivotal.literx.repository.ReactiveUserRepository;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

/**
 * Learn how to control the demand.
 *
 * @author Sebastien Deleuze
 */
public class Part06Request {

	ReactiveRepository<User> repository = new ReactiveUserRepository();

//========================================================================================

	// Create a StepVerifier that initially requests all values and expect 4 values to be received
	StepVerifier requestAllExpectFour(Flux<User> flux) {
		return StepVerifier.create(flux)
		.thenRequest(10)
		.expectNextCount(4)
		.expectComplete()
		.verifyLater();
	}

//========================================================================================

	// Create a StepVerifier that initially requests 1 value and expects User.SKYLER then requests another value and expects User.JESSE then stops verifying by cancelling the source
	StepVerifier requestOneExpectSkylerThenRequestOneExpectJesse(Flux<User> flux) {
		return StepVerifier.create(flux)
		.thenRequest(1)
		.expectNext(User.SKYLER)
		.thenRequest(1)
		.expectNext(User.JESSE)
		.thenCancel()
		.verifyLater();
	}

//========================================================================================

	// Return a Flux with all users stored in the repository that prints automatically logs for all Reactive Streams signals
	Flux<User> fluxWithLog() {
		return repository.findAll().log();
	}

//========================================================================================

	// Return a Flux with all users stored in the repository that prints "Starring:" on subscribe, "firstname lastname" for all values and "The end!" on complete
	Flux<User> fluxWithDoOnPrintln() {
		return repository.findAll()
		.doOnSubscribe(s -> System.out.println("Starring:"))
		.doOnNext(u -> System.out.println(u.getFirstname() + " " + u.getLastname()))
		.doOnComplete(()-> System.out.println("The end!"))
		;
	}

}
