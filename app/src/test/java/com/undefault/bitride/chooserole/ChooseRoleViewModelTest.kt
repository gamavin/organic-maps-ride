package com.undefault.bitride.chooserole

import android.content.Context
import com.undefault.bitride.MainDispatcherRule
import com.undefault.bitride.data.model.Roles
import com.undefault.bitride.data.repository.DataStoreRepository
import com.undefault.bitride.data.repository.LoggedInData
import com.undefault.bitride.data.repository.UserPreferencesRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito

@OptIn(ExperimentalCoroutinesApi::class)
class ChooseRoleViewModelTest {
  @get:Rule
  val dispatcherRule = MainDispatcherRule()

  private val context = Mockito.mock(Context::class.java)
  private val userRepo = Mockito.mock(UserPreferencesRepository::class.java)
  private val dataStore = Mockito.mock(DataStoreRepository::class.java)

  @Test
  fun noRolesCanRegisterBoth() = runTest {
    Mockito.`when`(userRepo.getLoggedInUser()).thenReturn(null)
    val vm = ChooseRoleViewModel(context, userRepo, dataStore)
    vm.refreshRoles()
    advanceUntilIdle()
    assertEquals(
      ChooseRoleUiState(canRegisterDriver = true, canRegisterCustomer = true),
      vm.uiState.value
    )
  }

  @Test
  fun customerRoleShowsLoginCustomer() = runTest {
    Mockito.`when`(userRepo.getLoggedInUser())
      .thenReturn(LoggedInData("id", listOf(Roles.CUSTOMER)))
    val vm = ChooseRoleViewModel(context, userRepo, dataStore)
    vm.refreshRoles()
    advanceUntilIdle()
    assertEquals(
      ChooseRoleUiState(canLoginAsCustomer = true, canRegisterDriver = true),
      vm.uiState.value
    )
  }

  @Test
  fun driverRoleShowsLoginDriver() = runTest {
    Mockito.`when`(userRepo.getLoggedInUser())
      .thenReturn(LoggedInData("id", listOf(Roles.DRIVER)))
    val vm = ChooseRoleViewModel(context, userRepo, dataStore)
    vm.refreshRoles()
    advanceUntilIdle()
    assertEquals(
      ChooseRoleUiState(canLoginAsDriver = true, canRegisterCustomer = true),
      vm.uiState.value
    )
  }

  @Test
  fun bothRolesShowBothLogins() = runTest {
    Mockito.`when`(userRepo.getLoggedInUser())
      .thenReturn(LoggedInData("id", listOf(Roles.DRIVER, Roles.CUSTOMER)))
    val vm = ChooseRoleViewModel(context, userRepo, dataStore)
    vm.refreshRoles()
    advanceUntilIdle()
    assertEquals(
      ChooseRoleUiState(canLoginAsDriver = true, canLoginAsCustomer = true),
      vm.uiState.value
    )
  }
}
