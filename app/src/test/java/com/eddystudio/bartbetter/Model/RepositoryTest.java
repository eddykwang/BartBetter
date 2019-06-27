package com.eddystudio.bartbetter.Model;

import com.eddystudio.bartbetter.Model.Response.EstimateResponse.EstimateResponse;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.observers.TestObserver;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RepositoryTest {

  private Repository repository;

  @Before
  public void setUp() throws Exception {
    repository = mock(Repository.class);
  }


  @Test
  public void testGetEstimateDateWithEmptyString() {
    when(repository.getEstimate("")).thenReturn(Observable.just(new EstimateResponse()));

    Observable<EstimateResponse> observable = repository.getEstimate("");
    TestObserver<EstimateResponse> testObserver =  observable.test();
    testObserver.assertNoErrors();
    testObserver.getEvents();
    testObserver.assertComplete();
  }

}