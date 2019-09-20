package com.njzz.bases.common;

import android.content.Context;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.SparseLongArray;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.njzz.bases.R;

import java.lang.ref.WeakReference;

public class RefreshableView extends LinearLayout /* implements View.OnTouchListener */ {

    public static final int STATUS_PULL_TO_REFRESH = 0;//下拉状态
    public static final int STATUS_RELEASE_TO_REFRESH = 1;//释放立即刷新状态
    public static final int STATUS_REFRESHING = 2;//正在刷新状态
    public static final int STATUS_REFRESH_FINISHED = 3;//刷新完成或未刷新状态
    private static SparseLongArray mSpLastUpdate=new SparseLongArray();//用于存储上次更新时间
    private int mCurrentStatus = STATUS_REFRESH_FINISHED;//当前处于什么状态
    private int mLastStatus = mCurrentStatus;//记录上一次的状态是什么，避免进行重复操作

    public static final int SCROLL_SPEED = -50;//下拉头部回滚的速度
    public static final long ONE_MINUTE = 60 * 1000;//一分钟的毫秒值，用于判断上次的更新时间
    public static final long ONE_HOUR = 60 * ONE_MINUTE;//一小时的毫秒值，用于判断上次的更新时间
    public static final long ONE_DAY = 24 * ONE_HOUR;// 一天的毫秒值，用于判断上次的更新时间
    //public static final long ONE_MONTH = 30 * ONE_DAY;// 一月的毫秒值，用于判断上次的更新时间
    //public static final long ONE_YEAR = 12 * ONE_MONTH;//一年的毫秒值，用于判断上次的更新时间
    private PullToRefreshListener mListener;//下拉刷新的回调接口
    private View mHeaderView;//下拉头的View
    private ProgressBar mProgressBar;//刷新时显示的进度条
    private ImageView mImgArrow;//指示下拉和释放的箭头
    private TextView mDescriptionTxt;//指示下拉和释放的文字描述
    private TextView mLastUpdateTxt;//上次更新时间的文字描述
    private MarginLayoutParams mHeaderLayoutParams;//下拉头的布局参数  (LinearLayout.LayoutParams 的父类)
    private int mId = -1;//为了防止不同界面的下拉刷新在上次更新时间上互相有冲突，使用id来做区分
    private int mHideHeaderHeight;//下拉头的高度
    private float mTouchYdown;//手指按下时的屏幕纵坐标
    //private int touchSlop;// 在被判定为滚动之前用户手指可以移动的最大值。
    private int mTouchSlop;//可以下拉的距离，这个距离以内不通知刷新，超出显示刷新
    private boolean mInProcessing;//当前是否可以下拉，只有ListView滚动到头的时候才允许下拉

    public RefreshableView(Context context){
        super(context);
        //internalInit(context);
    }
    /**
     * 下拉刷新控件的构造函数，会在运行时动态添加一个下拉头的布局。
     *
     * @param context
     * @param attrs
     */
    public RefreshableView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //internalInit(context);
    }

    private void internalInit(Context context){
        //preferences = PreferenceManager.getDefaultSharedPreferences(context);
        if(mHeaderView==null) {
            mHeaderView = LayoutInflater.from(context).inflate(R.layout.pull_to_refresh, null, false);
            mProgressBar = mHeaderView.findViewById(R.id.progress_bar);
            mImgArrow = mHeaderView.findViewById(R.id.arrow);
            mDescriptionTxt = mHeaderView.findViewById(R.id.description);
            mLastUpdateTxt = mHeaderView.findViewById(R.id.updated_at);
            //touchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
            refreshUpdatedAtValue();
            setOrientation(VERTICAL);
            addView(mHeaderView, 0);
        }
    }

    /**
     * 进行一些关键性的初始化操作，比如：将下拉头向上偏移进行隐藏。
     */
    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        if (changed && mHideHeaderHeight==0 && mHeaderView!=null ) {
            mHideHeaderHeight = -mHeaderView.getHeight();
            mHeaderLayoutParams = (MarginLayoutParams) mHeaderView.getLayoutParams();
            mHeaderLayoutParams.topMargin = mHideHeaderHeight;
            mHeaderView.setLayoutParams(mHeaderLayoutParams);

            //View v = getChildAt(1);
            //v.setOnTouchListener(this);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if(mInProcessing) return true;

        if(mListener!=null) {
            if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                mTouchYdown = ev.getRawY();
            } else if (ev.getAction() == MotionEvent.ACTION_MOVE) {//
                float yMove = ev.getRawY();
                if (yMove - mTouchYdown > 0) {//下拉
                    if( (mTouchSlop=mListener.canRefresh(mTouchYdown,yMove))> 0 ){
                        mInProcessing=true;
                        mTouchYdown = yMove;
                        return true;
                    }
                }
            }
        }

        return super.onInterceptTouchEvent(ev);
    }

    /**
     * 当ListView被触摸时调用，其中处理了各种下拉刷新的具体逻辑。
     */
    @Override
    public boolean onTouchEvent( MotionEvent event) {
        if (mInProcessing) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mTouchYdown = event.getRawY();
                    return true;
                   // break;
                case MotionEvent.ACTION_MOVE:
                    float yMove = event.getRawY();
                    int distance = (int) (yMove - mTouchYdown);
                    // 如果手指是下滑状态，并且下拉头是完全隐藏的，就屏蔽下拉事件
                    if (distance <= 0 && mHeaderLayoutParams.topMargin <= mHideHeaderHeight) {
                        return true;
                    }
//                    if (distance < touchSlop) {
//                        return true;
//                    }
                    if (mCurrentStatus != STATUS_REFRESHING) {
                        if (mHeaderLayoutParams.topMargin >= mTouchSlop) {
                            mCurrentStatus = STATUS_RELEASE_TO_REFRESH;
                        } else {
                            mCurrentStatus = STATUS_PULL_TO_REFRESH;
                        }
                        // 通过偏移下拉头的topMargin值，来实现下拉效果
                        mHeaderLayoutParams.topMargin = (distance / 2) + mHideHeaderHeight;
                        mHeaderView.setLayoutParams(mHeaderLayoutParams);
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    mInProcessing=false;
                //default:
                    if (mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
                        // 松手时如果是释放立即刷新状态，就去调用正在刷新的任务
                        new RefreshingTask(this).execute();
                    } else if (mCurrentStatus == STATUS_PULL_TO_REFRESH) {
                        // 松手时如果是下拉状态，就去调用隐藏下拉头的任务
                        new HideHeaderTask(this).execute();
                    }
                    break;
            }
            // 时刻记得更新下拉头中的信息
            if (mCurrentStatus == STATUS_PULL_TO_REFRESH
                    || mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
                updateHeaderView();
                // 当前正处于下拉或释放状态，要让ListView失去焦点，否则被点击的那一项会一直处于选中状态
//                listView.setPressed(false);
//                listView.setFocusable(false);
//                listView.setFocusableInTouchMode(false);
                mLastStatus = mCurrentStatus;
                // 当前正处于下拉或释放状态，通过返回true屏蔽掉其它事件
                return true;
            }
        }else{
            if (mHeaderLayoutParams!=null && mHeaderLayoutParams.topMargin != mHideHeaderHeight) {
                mHeaderLayoutParams.topMargin = mHideHeaderHeight;
                mHeaderView.setLayoutParams(mHeaderLayoutParams);
            }
        }
        return mInProcessing;
    }
    /**
     * 给下拉刷新控件注册一个监听器。
     *
     * @param listener
     *            监听器的实现。
     * @param id
     *            为了防止不同界面的下拉刷新在上次更新时间上互相有冲突， 请不同界面在注册下拉刷新监听器时一定要传入不同的id。
     */
    public void setOnRefreshListener(PullToRefreshListener listener, int id) {
        mListener = listener;
        mId = id;
        if(mListener!=null){
            internalInit(getContext());
        }
    }
    /**
     * 当所有的刷新逻辑完成后，记录调用一下，否则你的ListView将一直处于正在刷新状态。
     */
    public void finishRefreshing() {
        mCurrentStatus = STATUS_REFRESH_FINISHED;
        mSpLastUpdate.put(mId,System.currentTimeMillis());
        new HideHeaderTask(this).execute();
    }

    /**
     * 更新下拉头中的信息。
     */
    private void updateHeaderView() {
        if (mLastStatus != mCurrentStatus) {
            if (mCurrentStatus == STATUS_PULL_TO_REFRESH) {
                mDescriptionTxt.setText("下拉可以刷新");
                mImgArrow.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                rotateArrow();
            } else if (mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
                mDescriptionTxt.setText("释放立即刷新");
                mImgArrow.setVisibility(View.VISIBLE);
                mProgressBar.setVisibility(View.GONE);
                rotateArrow();
            } else if (mCurrentStatus == STATUS_REFRESHING) {
                mDescriptionTxt.setText("正在刷新...");
                mProgressBar.setVisibility(View.VISIBLE);
                mImgArrow.clearAnimation();
                mImgArrow.setVisibility(View.GONE);
            }
            refreshUpdatedAtValue();
        }
    }

    /**
     * 根据当前的状态来旋转箭头。
     */
    private void rotateArrow() {
        float pivotX = mImgArrow.getWidth() / 2f;
        float pivotY = mImgArrow.getHeight() / 2f;
        float fromDegrees = 0f;
        float toDegrees = 0f;
        if (mCurrentStatus == STATUS_PULL_TO_REFRESH) {
            fromDegrees = 180f;
            toDegrees = 360f;
        } else if (mCurrentStatus == STATUS_RELEASE_TO_REFRESH) {
            fromDegrees = 0f;
            toDegrees = 180f;
        }
        RotateAnimation animation = new RotateAnimation(fromDegrees, toDegrees, pivotX, pivotY);
        animation.setDuration(100);
        animation.setFillAfter(true);
        mImgArrow.startAnimation(animation);
    }

    /**
     * 刷新下拉头中上次更新时间的文字描述。
     */
    private void refreshUpdatedAtValue() {
        long lastUpdateTime = mSpLastUpdate.get( mId);
        long currentTime = System.currentTimeMillis();
        long timePassed = currentTime - lastUpdateTime;
        long timeIntoFormat;
        String updateAtValue;
        String updateBy="上次更新于%1$s前";
        if (lastUpdateTime == 0) {
            updateAtValue = "暂未更新过";
        }  else if (timePassed < ONE_MINUTE) {
            updateAtValue = "刚刚更新";
        } else if (timePassed < ONE_HOUR) {
            timeIntoFormat = timePassed / ONE_MINUTE;
            String value = timeIntoFormat + "分钟";
            updateAtValue = String.format(updateBy, value);
        } else if (timePassed < ONE_DAY) {
            timeIntoFormat = timePassed / ONE_HOUR;
            String value = timeIntoFormat + "小时";
            updateAtValue = String.format(updateBy, value);
        } else {
            timeIntoFormat = timePassed / ONE_DAY;
            String value = timeIntoFormat + "天";
            updateAtValue = String.format(updateBy, value);
        }
        mLastUpdateTxt.setText(updateAtValue);
    }

    /**
     * 正在刷新的任务，在此任务中会去回调注册进来的下拉刷新监听器。
     *
     * @author guolin
     */
    static class RefreshingTask extends AsyncTask<Void, Integer, Void> {

        private WeakReference<RefreshableView> mReference;

        RefreshingTask(RefreshableView v){
            mReference=new WeakReference<>(v);
        }
        RefreshableView refreshView() {return mReference.get();}

        @Override
        protected Void doInBackground(Void... params) {
            RefreshableView view=refreshView();
            if (view!=null) {
                int topMargin = view.mHeaderLayoutParams.topMargin;
                while (true) {
                    topMargin = topMargin + SCROLL_SPEED;
                    if (topMargin <= 0) {
                        break;
                    }
                    publishProgress(topMargin);
                    sleep(10);
                }
                view.mCurrentStatus = STATUS_REFRESHING;
                publishProgress(0);
                if (view.mListener != null) {
                    view.mListener.onRefresh();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... topMargin) {
            RefreshableView view=refreshView();
            if (view!=null) {
                view.updateHeaderView();
                view.mHeaderLayoutParams.topMargin = topMargin[0];
                view.mHeaderView.setLayoutParams(view.mHeaderLayoutParams);
            }
        }

    }

    /**
     * 隐藏下拉头的任务，当未进行下拉刷新或下拉刷新完成后，此任务将会使下拉头重新隐藏。
     *
     * @author guolin
     */
    static class HideHeaderTask extends AsyncTask<Void, Integer, Integer> {

        private WeakReference<RefreshableView> mReference;
        HideHeaderTask(RefreshableView v){
            mReference=new WeakReference<>(v);
        }
        RefreshableView refreshView() {return mReference.get();}

        @Override
        protected Integer doInBackground(Void... params) {
            RefreshableView view = refreshView();
            int topMargin=0;
            if(view!=null) {
                topMargin = view.mHeaderLayoutParams.topMargin;
                while (true) {
                    topMargin = topMargin + SCROLL_SPEED;
                    if (topMargin <= view.mHideHeaderHeight) {
                        topMargin = view.mHideHeaderHeight;
                        break;
                    }
                    publishProgress(topMargin);
                    sleep(10);
                }
            }
            return topMargin;
        }

        @Override
        protected void onProgressUpdate(Integer... topMargin) {
            RefreshableView view = refreshView();
            if(view!=null) {
                view.mHeaderLayoutParams.topMargin = topMargin[0];
                view.mHeaderView.setLayoutParams(view.mHeaderLayoutParams);
            }
        }

        @Override
        protected void onPostExecute(Integer topMargin) {
            RefreshableView view = refreshView();
            if(view!=null) {
                view.mHeaderLayoutParams.topMargin = topMargin;
                view.mHeaderView.setLayoutParams(view.mHeaderLayoutParams);
                view.mCurrentStatus = STATUS_REFRESH_FINISHED;
            }
        }
    }

    /**
     * 使当前线程睡眠指定的毫秒数。
     *
     * @param time
     *            指定当前线程睡眠多久，以毫秒为单位
     */
    static private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /////////////////////////////////////////////////////////////////////////////////////////////////////////////
    /**
     * 下拉刷新的监听器，使用下拉刷新的地方应该注册此监听器来获取刷新回调。
     *
     * @author guolin
     */
    public interface PullToRefreshListener {
        /**
         * 刷新时会去回调此方法，在方法内编写具体的刷新逻辑。此方法是在子线程中调用的， 可以不必另开线程来进行耗时操作。
         */
        void onRefresh();
        //是否可以刷新
        //返回 > 0 显示可刷新的下拉距离，返回<=0 不能刷新
        int canRefresh(float downPos,float nowPos);
    }

//    public static boolean listViewTestRefresh(ListView lv){
//        if( lv ==null )return false;
//        View firstChild = lv.getChildAt(0);
//        if (firstChild != null) {
//            int firstVisiblePos = lv.getFirstVisiblePosition();
//            // 如果首个元素的上边缘，距离父布局值为0，就说明ListView滚动到了最顶部，此时应该允许下拉刷新
//            return firstVisiblePos == 0 && firstChild.getTop() == 0;
//        } else {
//            // 如果ListView中没有元素，也应该允许下拉刷新
//           return true;
//        }
//    }

}

