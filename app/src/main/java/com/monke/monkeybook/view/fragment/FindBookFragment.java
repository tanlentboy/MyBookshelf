package com.monke.monkeybook.view.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.monke.monkeybook.R;
import com.monke.monkeybook.base.MBaseFragment;
import com.monke.monkeybook.bean.FindKindBean;
import com.monke.monkeybook.bean.FindKindGroupBean;
import com.monke.monkeybook.presenter.FindBookPresenterImpl;
import com.monke.monkeybook.presenter.contract.FindBookContract;
import com.monke.monkeybook.view.activity.ChoiceBookActivity;
import com.monke.monkeybook.view.adapter.FindKindAdapter;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FindBookFragment extends MBaseFragment<FindBookContract.Presenter> implements FindBookContract.View {
    @BindView(R.id.ll_content)
    LinearLayout llContent;
    @BindView(R.id.expandable_list)
    ExpandableListView expandableList;
    @BindView(R.id.tv_empty)
    TextView tvEmpty;
    @BindView(R.id.refresh_layout)
    SwipeRefreshLayout refreshLayout;

    Unbinder unbinder;

    private FindKindAdapter adapter;

    @Override
    public int createLayoutId() {
        return R.layout.fragment_expandable_list_vew;
    }

    @Override
    protected void bindView() {
        super.bindView();
        ButterKnife.bind(this, view);
        initExpandableList();
        refreshLayout.setColorSchemeColors(getResources().getColor(R.color.colorAccent));
        refreshLayout.setOnRefreshListener(() -> {
            mPresenter.initData();
            refreshLayout.setRefreshing(false);
        });
    }

    @Override
    protected void initData() {
        mPresenter.initData();
    }

    @Override
    protected FindBookContract.Presenter initInjector() {
        return new FindBookPresenterImpl();
    }

    @Override
    public synchronized void updateUI(List<FindKindGroupBean> group) {
        if (group.size() > 0) {
            adapter.resetDataS(group);
            if (autoExpandGroup() || group.size() == 1) {
                expandableList.expandGroup(0);
            }
        }
    }

    private boolean autoExpandGroup() {
        return preferences.getBoolean(getString(R.string.pk_find_expand_group), false);
    }

    private void initExpandableList() {
        adapter = new FindKindAdapter(getActivity());
        expandableList.setAdapter(adapter);
        tvEmpty.setText(R.string.find_empty);
        expandableList.setEmptyView(tvEmpty);
        adapter.setOnGroupExpandedListener(this::expandOnlyOne);
        //  设置分组项的点击监听事件
        expandableList.setOnGroupClickListener((parent, v, groupPosition, id) -> {
            // 请务必返回 false，否则分组不会展开
            return false;
        });

        //  设置子选项点击监听事件
        expandableList.setOnChildClickListener((parent, v, groupPosition, childPosition, id) -> {
            FindKindBean kindBean = adapter.getDataList().get(groupPosition).getChildren().get(childPosition);

            Intent intent = new Intent(getContext(), ChoiceBookActivity.class);
            intent.putExtra("url", kindBean.getKindUrl());
            intent.putExtra("title", kindBean.getKindName());
            intent.putExtra("tag", kindBean.getTag());
            startActivityByAnim(intent, v, "sharedView", android.R.anim.fade_in, android.R.anim.fade_out);
            return true;
        });
    }

    // 每次展开一个分组后，关闭其他的分组
    private boolean expandOnlyOne(int expandedPosition) {
        boolean result = true;
        int groupLength = expandableList.getExpandableListAdapter().getGroupCount();
        for (int i = 0; i < groupLength; i++) {
            if (i != expandedPosition && expandableList.isGroupExpanded(i)) {
                result &= expandableList.collapseGroup(i);
            }
        }
        return result;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // TODO: inflate a fragment view
        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        unbinder = ButterKnife.bind(this, rootView);
        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
