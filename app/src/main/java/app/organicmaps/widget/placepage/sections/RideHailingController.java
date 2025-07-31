package app.organicmaps.widget.placepage.sections;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import java.util.ArrayList;
import app.organicmaps.MwmActivity;
import app.organicmaps.R;
import app.organicmaps.sdk.Framework;
import app.organicmaps.sdk.bookmarks.data.MapObject;
import app.organicmaps.util.bottomsheet.MenuBottomSheetFragment;
import app.organicmaps.util.bottomsheet.MenuBottomSheetItem;
import app.organicmaps.widget.placepage.PlacePageButtons;
import app.organicmaps.widget.placepage.PlacePageView;
import app.organicmaps.widget.placepage.PlacePageViewModel;

public class RideHailingController
        extends Fragment implements PlacePageView.PlacePageViewListener, PlacePageButtons.PlacePageButtonClickListener,
        MenuBottomSheetFragment.MenuBottomSheetInterface, Observer<MapObject>
{
  private BottomSheetBehavior<View> mPlacePageBehavior;
  private NestedScrollView mPlacePage;
  private PlacePageViewModel mViewModel;
  @Nullable
  private MapObject mMapObject;

  private final BottomSheetBehavior.BottomSheetCallback mDefaultBottomSheetCallback =
          new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState)
            {
              if (newState == BottomSheetBehavior.STATE_HIDDEN)
                onHiddenInternal();
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset)
            {
              // Do nothing in this simplified controller
            }
          };

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState)
  {
    return inflater.inflate(R.layout.place_page_container_fragment, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
  {
    super.onViewCreated(view, savedInstanceState);

    mPlacePage = view.findViewById(R.id.placepage);
    mPlacePageBehavior = BottomSheetBehavior.from(mPlacePage);
    mViewModel = new ViewModelProvider(requireActivity()).get(PlacePageViewModel.class);

    mPlacePageBehavior.setHideable(true);
    mPlacePageBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
  }

  private void onHiddenInternal()
  {
    Framework.nativeDeactivatePopup();
    removePlacePageFragments();
  }

  private void close()
  {
    mPlacePageBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
  }

  private void removePlacePageFragments()
  {
    final FragmentManager fm = getChildFragmentManager();
    final Fragment placePageButtonsFragment = fm.findFragmentByTag("PLACE_PAGE_BUTTONS");
    final Fragment placePageFragment = fm.findFragmentByTag("PLACE_PAGE");

    if (placePageButtonsFragment != null)
      fm.beginTransaction().remove(placePageButtonsFragment).commit();
    if (placePageFragment != null)
      fm.beginTransaction().remove(placePageFragment).commit();

    mViewModel.setMapObject(null);
  }

  private void createPlacePageFragments()
  {
    final FragmentManager fm = getChildFragmentManager();
    if (fm.findFragmentByTag("PLACE_PAGE") == null)
    {
      fm.beginTransaction()
              .add(R.id.placepage_fragment, PlacePageView.class, null, "PLACE_PAGE")
              .commit();
    }
    if (fm.findFragmentByTag("PLACE_PAGE_BUTTONS") == null)
    {
      fm.beginTransaction()
              .add(R.id.pp_buttons_fragment, PlacePageButtons.class, null, "PLACE_PAGE_BUTTONS")
              .commit();
    }
  }

  private void updateButtons(MapObject mapObject, boolean showBackButton, boolean showRoutingButton)
  {
    mViewModel.setCurrentButtons(new ArrayList<>());
  }

  @Override
  public void onChanged(@Nullable MapObject mapObject)
  {
    mMapObject = mapObject;
    if (mapObject != null)
    {
      createPlacePageFragments();
      updateButtons(mapObject, false, false);

      mPlacePageBehavior.setDraggable(false);
      mPlacePage.post(() -> {
        mPlacePageBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mPlacePageBehavior.setPeekHeight(mPlacePage.getHeight());
      });
    }
    else
    {
      close();
    }
  }

  @Override
  public void onStart()
  {
    super.onStart();
    mPlacePageBehavior.addBottomSheetCallback(mDefaultBottomSheetCallback);
    mViewModel.getMapObject().observe(requireActivity(), this);
  }

  @Override
  public void onStop()
  {
    super.onStop();
    mPlacePageBehavior.removeBottomSheetCallback(mDefaultBottomSheetCallback);
    mViewModel.getMapObject().removeObserver(this);
  }

  // Implementasi kosong untuk metode-metode interface yang tidak terpakai
  @Override
  public void onPlacePageContentChanged(int previewHeight, int frameHeight) {}
  @Override
  public void onPlacePageRequestToggleState() {}
  @Override
  public void onPlacePageRequestClose() {}
  @Override
  public void onPlacePageButtonClick(PlacePageButtons.ButtonType item) {}
  @Nullable
  @Override
  public ArrayList<MenuBottomSheetItem> getMenuBottomSheetItems(String id) { return null; }
}