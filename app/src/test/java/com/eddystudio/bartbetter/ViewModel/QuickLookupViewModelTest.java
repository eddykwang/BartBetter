package com.eddystudio.bartbetter.ViewModel;

import com.eddystudio.bartbetter.Model.Repository;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Observable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuickLookupViewModelTest {
  private QuickLookupViewModel quickLookupViewModel;
  private Repository repository;

  @Before
  public void setUp() throws Exception {
    quickLookupViewModel = mock(QuickLookupViewModel.class);
    repository = mock(Repository.class);
  }

  @Test
  public void getEventsSubject() {
    Observable<Events> observable = (Observable<Events>) mock(Observable.class);
    when(quickLookupViewModel.getEventsSubject()).thenReturn(observable);

    quickLookupViewModel.getEventsSubject();
    verify(quickLookupViewModel).getEventsSubject();
  }

  @Test
  public void getData() {
    quickLookupViewModel.getData(null);
    verify(quickLookupViewModel).getData(null);
  }
}