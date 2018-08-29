package com.eddystudio.bartbetter.ViewModel;

import com.eddystudio.bartbetter.Model.Repository;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;
import io.reactivex.Scheduler;
import io.reactivex.schedulers.TestScheduler;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuickLookupViewModelTest {
  private QuickLookupViewModel quickLookupViewModel;
  private Repository repository;
  private Scheduler scheduler;

  @Before
  public void setUp() throws Exception {
    quickLookupViewModel = mock(QuickLookupViewModel.class);
    repository = mock(Repository.class);
    scheduler = new TestScheduler();
  }

  @Test
  public void getEventsSubject() {
    Observable<Events> observable = (Observable<Events>) mock(Observable.class);
    when(quickLookupViewModel.getEventsSubject()).thenReturn(observable);

    quickLookupViewModel.getEventsSubject();
    verify(quickLookupViewModel).getEventsSubject();
  }

  @Test
  public void getDataWithEmptyStation() {
    doReturn(null).when(repository).getListEstimate(null);
    quickLookupViewModel.getData(null);
    scheduler.start();
    verify(quickLookupViewModel).getData(null);
  }


  @Test
  public void getDataWithCorrectStation() {
    doReturn(Observable.just("test")).when(repository).getEstimate("a");
    quickLookupViewModel.getData("a");
    scheduler.start();
    verify(quickLookupViewModel).getData("a");
  }
}