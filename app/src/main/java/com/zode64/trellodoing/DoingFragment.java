package com.zode64.trellodoing;

import android.app.DialogFragment;
import android.os.Bundle;

public class DoingFragment extends DialogFragment {

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setStyle( DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog );
    }
}
