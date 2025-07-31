package app.organicmaps.bookmarks;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import app.organicmaps.R;
import app.organicmaps.sdk.bookmarks.data.BookmarkCategory;
import app.organicmaps.sdk.bookmarks.data.BookmarkManager;
import app.organicmaps.widget.recycler.DividerItemDecorationWithPadding;

// PERBAIKAN: Tidak lagi menggunakan BaseMwmRecyclerFragment, tapi Fragment dasar.
public class RidehailingBookmarksFragment extends Fragment
{
    private RecyclerView mRecycler;
    private BookmarkCategoriesAdapter mAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        // Layout XML kita sudah benar, tidak perlu diubah.
        return inflater.inflate(R.layout.fragment_ridehailing_bookmarks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState)
    {
        super.onViewCreated(view, savedInstanceState);

        // Menyiapkan daftar (RecyclerView) secara manual karena tidak memakai cetakan.
        mRecycler = view.findViewById(R.id.recycler);
        mRecycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        mRecycler.addItemDecoration(new DividerItemDecorationWithPadding(requireContext()));

        // Mengambil daftar kategori bookmark dari sistem.
        List<BookmarkCategory> categories = BookmarkManager.INSTANCE.getCategories();
        // Membuat adapter dengan cara yang benar.
        mAdapter = new BookmarkCategoriesAdapter(requireContext(), categories);
        // Mengatur aksi klik.
        mAdapter.setOnClickListener((v, item) -> {
            final Intent intent = new Intent(requireActivity(), BookmarkListActivity.class);
            intent.putExtra(BookmarksListFragment.EXTRA_CATEGORY, item);
            startActivity(intent);
        });

        // Memasang adapter ke RecyclerView.
        mRecycler.setAdapter(mAdapter);
    }
}