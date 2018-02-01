package com.jyuesong.dragview2fill.dragview;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jyuesong.dragview2fill.FastModel;
import com.jyuesong.dragview2fill.R;
import com.jyuesong.dragview2fill.adapter.BaseAdapter;
import com.jyuesong.dragview2fill.adapter.BaseViewHolder;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by jiang on 29/01/2018.
 */
public class RecyclerViewHelper extends ItemTouchHelper.Callback {


    private ViewGroup mParent;
    private RecyclerView mRecyclerView;
    private ItemTouchHelper mItemTouchHelper;
    public static final int MAX_COUNT = 50; //题数最大值
    public static final int MAX_SUBJECT_COUNT = 10; //可拖动的最大个数
    public SizeChangedCallBack mSizeChangedCallBack;
    protected String mSubjectTitle = getSubjectTitle();

    public void setSizeChangedCallBack(SizeChangedCallBack sizeChangedCallBack) {
        mSizeChangedCallBack = sizeChangedCallBack;
    }

    public RecyclerViewHelper(ViewGroup parent) {
        this.mParent = parent;

    }

    public static RecyclerViewHelper create(ViewGroup parent) {
        return new RecyclerViewHelper(parent);
    }

    public RecyclerView generateRecyclerView() {
        mRecyclerView = new RecyclerView(mParent.getContext());
        mItemTouchHelper = new ItemTouchHelper(this);
        mItemTouchHelper.attachToRecyclerView(mRecyclerView);
        return mRecyclerView;
    }

    public RecyclerView getRecyclerView() {
        return mRecyclerView;
    }

    private int lastPosition = -1;
    private boolean dealing = false;

    public List<FastModel> mList = new ArrayList<>();


    public void init(List<FastModel> lianXiTiXingModels) {
        mList.clear();
        mList.add(new FastModel(DragViewGroup.T_TITLE, 0));
        if (lianXiTiXingModels == null) {
            mList.add(new FastModel(DragViewGroup.T_EMPTY, 0));
        } else {
            mList.addAll(lianXiTiXingModels);
        }

        mRecyclerView.setLayoutManager(new LinearLayoutManager(mParent.getContext(), LinearLayoutManager.VERTICAL, false));
        mRecyclerView.setAdapter(new MyAdapter());
    }

    public void init() {
        init(null);


    }

    public void adding(int pos) {
        if (lastPosition == pos) return;
        if (pos < 0) return;
        cancel();
        mList.add(pos, new FastModel(true));
        lastPosition = pos;
        mRecyclerView.getAdapter().notifyItemInserted(pos);

    }

    public void dragFinish(int tag) {

        int bestPosition = lastPosition;
        cancel();
        if (bestPosition >= 0) {
            mList.add(bestPosition, new FastModel(tag, 1));
            mRecyclerView.getAdapter().notifyItemInserted(bestPosition);
            if (mSizeChangedCallBack != null) {
                mSizeChangedCallBack.size(mList.size() - 1);
            }
        }
        reset();
        if (mList.size() > MAX_SUBJECT_COUNT) {
            msg("题型最多添加" + MAX_SUBJECT_COUNT + "个");
        }
    }

    private void removeAddingItem() {
        for (int i = 0; i < mList.size(); i++) {
            if (mList.get(i).isAdding()) {
                mList.remove(i);
                i--;
            }

        }
    }

    public void cancel() {
        lastPosition = -1;
        if (mList.size() == 2 && mList.get(1).getType() == DragViewGroup.T_EMPTY) mList.remove(1);
        removeAddingItem();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    public void reset() {
        dealing = false;
        lastPosition = -1;

    }

    public static String getSubjectTitle() {
        return "课堂练习 " + new SimpleDateFormat("yyyyMMdd HH:mm").format(new Date());

    }

    public void draging(int[] ints) {

        if (dealing) return;
        dealing = true;
        //只需要知道顶部坐标，来判断要不要添加，添加到哪
        int top = ints[1];
        int bottom = ints[2];
        int tag = ints[3];

        //根据position一个一个找到一个位置，位置的顶部在top上面，然后下一个位置的底部在top的下面
        int positions = findBestPosition(top, bottom, mRecyclerView);
        if (positions == 0) {
            positions = 1;
        }
        if (mList.size() == 1 && mList.get(0).isAdding()) return;
        adding(positions);
        dealing = false;
    }

    private int findBestPosition(int top, int bottom, RecyclerView recyclerView) {
        if (mList == null || mList.size() == 0) return 0;
        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();

        int position = linearLayoutManager.findFirstVisibleItemPosition();


        View view = linearLayoutManager.findViewByPosition(position);

        if (view == null) return -1;
        if (view.getTop() > top && view.getBottom() > bottom) {
            return position;
        }
        for (int i = position; i < mList.size() - 1; i++) {
            View view1 = linearLayoutManager.findViewByPosition(i);
            View view2 = linearLayoutManager.findViewByPosition(i + 1);
            if (view1 == null || view2 == null) {
                return -1;
            }
            int tempTop = view1.getTop();
            int tempBottom = view2.getBottom();
            if (tempBottom > top && tempTop < top) {
                return i + 1;
            }
        }
        return position + 1;
    }

    private static final String TAG = "RecyclerViewHelper";

    @Override
    public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        if (mList.get(position).getType() == DragViewGroup.T_TITLE) return 0;
        if (mList.get(position).getType() == DragViewGroup.T_EMPTY)
            return makeMovementFlags(ItemTouchHelper.UP |
                    ItemTouchHelper.DOWN, 0);
        if (recyclerView.getLayoutManager() instanceof LinearLayoutManager) {
            final int dragFlag = ItemTouchHelper.UP |
                    ItemTouchHelper.DOWN | ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT;
            int swipeFlag = ItemTouchHelper.START;
            return makeMovementFlags(dragFlag, swipeFlag);
        }
        return 0;
    }

    @Override
    public boolean isItemViewSwipeEnabled() {
        return true;
    }

    @Override
    public boolean isLongPressDragEnabled() {
        return true;
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
        int fromPos = viewHolder.getAdapterPosition();
        int toPos = target.getAdapterPosition();
        if (toPos == 0) toPos = 1;
        if (fromPos == toPos) return true;
        if (fromPos < toPos) {
            for (int i = fromPos; i < toPos; i++) {
                Collections.swap(mList, i, i + 1);
            }
        } else {
            for (int i = fromPos; i > toPos; i--) {
                Collections.swap(mList, i, i - 1);
            }
        }

        recyclerView.getAdapter().notifyItemMoved(fromPos, toPos);
        if (mSizeChangedCallBack != null) {
            mSizeChangedCallBack.size(mList.size() - 1);
        }
        return true;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (mList.get(position).getType() == DragViewGroup.T_TITLE) return;
        deleteItem(position, true);

    }


    public class MyAdapter extends BaseAdapter {
        @Override
        public void onBindView(final BaseViewHolder holder, final int position) {
            if (mList.get(position).isAdding()) return;

            int type = mList.get(position).getType();
            if (type == DragViewGroup.T_TITLE) {
                holder.getConvertView().setBackgroundColor(Color.parseColor("#ffffff"));
                holder.setText(R.id.fast_new_title, mSubjectTitle);

            } else if (type == DragViewGroup.T_EMPTY) {
            } else {
                holder.getConvertView().setBackgroundColor(Color.parseColor("#f7f7f9"));
                final int countValue = mList.get(position).getCount();
                String title = getTitleByType(type);
                ImageView delete = holder.getView(R.id.fast_new_delete);
                delete.setClickable(true);
                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        deleteItem(position, false);

                    }
                });
                holder.setText(R.id.title, title);
                ImageView imageView = holder.getView(R.id.fast_item_icon);
                int drawable = getDrawableByType(type);
                imageView.setImageDrawable(ContextCompat.getDrawable(mParent.getContext(), drawable));
                TextView count = holder.getView(R.id.fast_count);
                count.setText(countValue + "");
                count.setClickable(true);
                count.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        itemCountClicked(position, mList.get(position).getCount());
                    }
                });

                ImageView add = holder.getView(R.id.fast_add);
                ImageView reduce = holder.getView(R.id.fast_reduce);
                if (countValue >= MAX_COUNT) {
                    add.setVisibility(View.GONE);
                } else {
                    add.setVisibility(View.VISIBLE);
                    add.setClickable(true);
                    add.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mList.get(position).setCount(countValue + 1);
                            notifyItemChanged(position);
                        }
                    });
                }
                if (countValue <= 1) {
                    reduce.setVisibility(View.GONE);
                } else {
                    reduce.setVisibility(View.VISIBLE);
                    reduce.setClickable(true);
                    reduce.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mList.get(position).setCount(countValue - 1);
                            notifyItemChanged(position);
                        }
                    });
                }

            }

        }

        @Override
        public int getLayoutID(int position) {
            if (mList.get(position).isAdding()) {
                return R.layout.fast_adding;

            }
            if (mList.get(position).getType() == DragViewGroup.T_TITLE)
                return R.layout.fast_item_title;
            if (mList.get(position).getType() == DragViewGroup.T_EMPTY)
                return R.layout.fast_item_empty;
            return R.layout.fast_tixing_item;
        }

        @Override
        public boolean clickable() {
            return false;
        }

        @Override
        public int getItemCount() {
            return mList.size();
        }
    }

    private void deleteItem(int position, boolean anim) {
        mList.remove(position);
        if (anim) {
            mRecyclerView.getAdapter().notifyItemRemoved(position);
        }
        if (mList.size() == 1) {
            mList.add(new FastModel(DragViewGroup.T_EMPTY, 0));
            mRecyclerView.getAdapter().notifyItemInserted(1);

        }
        //以后可以用 notifyItemRangeChanged这个方法来提高效率
//        if (!anim) {
        mRecyclerView.getAdapter().notifyDataSetChanged();
//        }
        if (mSizeChangedCallBack != null) {
            if (mList.size() == 2 && mList.get(1).getType() == DragViewGroup.T_EMPTY) {
                mSizeChangedCallBack.size(0);
            } else {
                mSizeChangedCallBack.size(mList.size() - 1);
            }
        }
    }

    private void itemCountClicked(final int position, int count) {
        final EditText input = new EditText(mParent.getContext());
        input.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(3)});

        // input.setImeOptions(EditorInfo.IME_ACTION_DONE);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        input.setText(count + "");

        final AlertDialog alertDialog = new AlertDialog.Builder(mParent.getContext())
                .setView(input)
                .setCancelable(false)
                .setTitle("请设置题数")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                    }
                }).create();
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Editable editable = input.getText();
                if (TextUtils.isEmpty(editable)) {
                    msg("题数不能为空");
                    return;
                }
                try {
                    int result = Integer.valueOf(editable.toString());
                    if (result < 0) {
                        msg("题数不能小于0");
                        return;
                    }
                    if (result == 0) {
                        msg("题数不能为0");
                        return;
                    }
                    if (result > MAX_COUNT) {
                        msg("题数不能超过" + MAX_COUNT);
                        return;
                    }
                    mList.get(position).setCount(result);
                    alertDialog.dismiss();
                    mRecyclerView.getAdapter().notifyItemChanged(position);
                } catch (Exception e1) {
                    msg("题数不合法");


                }

            }
        });

    }

    private void msg(String s) {
        Toast.makeText(mParent.getContext(), s, Toast.LENGTH_SHORT).show();
    }

    private int getDrawableByType(int type) {
        switch (type) {
            case DragViewGroup.T_PANDUAN:
                return R.mipmap.fast_panduan_icon;

            case DragViewGroup.T_XUANZE:
                return R.mipmap.fast_xuanze_icon;

            case DragViewGroup.T_ZHUGUAN:
                return R.mipmap.fast_zhuguan_icon;
        }
        return R.mipmap.fast_zhuguan_icon;
    }

    private String getTitleByType(int type) {
        switch (type) {
            case DragViewGroup.T_PANDUAN:
                return "判断题";

            case DragViewGroup.T_XUANZE:
                return "选择题";

            case DragViewGroup.T_ZHUGUAN:
                return "主观题";
        }
        return "大题";
    }

    public interface SizeChangedCallBack {
        void size(int count);

    }
}
