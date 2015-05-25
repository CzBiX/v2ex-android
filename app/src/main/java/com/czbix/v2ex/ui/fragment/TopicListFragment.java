package com.czbix.v2ex.ui.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.adapter.TopicAdapter;
import com.czbix.v2ex.ui.loader.TopicLoader;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TopicListFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TopicListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TopicListFragment extends Fragment implements LoaderManager.LoaderCallbacks<List<Topic>>,TopicAdapter.OnItemClickListener {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;
    private LinearLayoutManager mLayoutManager;
    private RecyclerView mRecyclerView;
    private TopicAdapter mAdapter;
    private View mProgressBar;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment TopicListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static TopicListFragment newInstance(String param1, String param2) {
        TopicListFragment fragment = new TopicListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public TopicListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_topic_list,
                container, false);

        mRecyclerView = ((RecyclerView) view.findViewById(R.id.recycle_view));
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(view.getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new TopicAdapter(this);
        mRecyclerView.setAdapter(mAdapter);

        mProgressBar = view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public Loader<List<Topic>> onCreateLoader(int id, Bundle args) {
        return new TopicLoader(getActivity());
    }

    @Override
    public void onLoadFinished(Loader<List<Topic>> loader, List<Topic> data) {
        mAdapter.setDataSource(data);
        showTopicList();
    }

    private void showTopicList() {
        mProgressBar.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_out));
        mRecyclerView.startAnimation(AnimationUtils.loadAnimation(getActivity(), android.R.anim.fade_in));
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public void onLoaderReset(Loader<List<Topic>> loader) {
        mAdapter.setDataSource(null);
    }

    @Override
    public void onItemClick(int position, Topic topic) {
        mListener.onFragmentInteraction(topic);
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Topic topic);
    }

}
