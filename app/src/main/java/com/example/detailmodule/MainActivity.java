package com.example.detailmodule;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.TextView;

import com.example.detailmodule.fragments.DetailFragment;
import com.example.detailmodule.fragments.RedPaperFragment;
import com.example.detailmodule.utils.ParamsUtil;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    ConstraintLayout mConstraintLayout;
    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        getWindow().setAttributes(layoutParams);

        // Example of a call to a native method
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());
        mConstraintLayout = findViewById(R.id.basePanel);
        mConstraintLayout.removeAllViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"onConfigurationChanged,onResume");
        testLuckyView();
//        testDetailView();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG,"onConfigurationChanged,newConfig="+newConfig);
    }

    private void testLuckyView() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        RedPaperFragment redPaperFragment = new RedPaperFragment("http://pic.sc.chinaz.com/files/pic/pic9/202008/bpic21109.jpg", "","自然石头堆叠图片");
        fragmentTransaction.add(redPaperFragment,"redPaper");
        fragmentTransaction.show(redPaperFragment);
        fragmentTransaction.commit();
    }

    private void testDetailView() {
        ParamsUtil.DETAIL_TITLE = "欧洲古建筑夜景图片";
        ParamsUtil.DETAIL_DESCRIPTION = "欧洲建筑是分布在欧洲的古代建筑的统称。其风格在建造形态上的特点是：简洁、线条分明、讲究对称、运用色彩的明暗,鲜淡来对视觉进行冲击.在意态上则使人感到壅容华贵.典雅,富有浪漫主义色彩。\n" +
                "欧洲建筑风格包括：巴洛克建筑、法国古典主义建筑、哥特式建筑、古罗马建筑、浪漫主义建筑、罗曼建筑、洛克克风格、文艺复兴建筑。古罗马人沿袭亚平宁半岛上伊特鲁里亚人的建筑技术，继承古希腊成就，在公元1-3世纪达到西方古代建筑极盛高峰。大型建筑物风格雄浑凝重，构图和谐统一，形式多样。有些建筑物内部空间艺术处理的重要性超过了外部体形。最有意义的是创造出柱式同拱券的组合，如券柱式和连续券，既作结构，又作装饰。古罗马建筑的类型很多。有罗马万神庙、维纳斯和罗马庙，以及巴尔贝克太阳神庙等宗教建筑，也有皇宫、剧场角斗场、浴场以及广场和巴西利卡(长方形会堂)等公共建筑";
        ParamsUtil.DETAIL_BITMAP_URL = "http://pic2.sc.chinaz.com/files/pic/pic9/202008/apic27361.jpg";
        ParamsUtil.DETAIL_AUDIO_URL = null;
        ParamsUtil.DETAIL_PANORAMA_URL = null;
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        DetailFragment detailFragment = new DetailFragment();
        fragmentTransaction.add(detailFragment,"detail");
        fragmentTransaction.show(detailFragment);
        fragmentTransaction.commit();
    }
    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();
}
