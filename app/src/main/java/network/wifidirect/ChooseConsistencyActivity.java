package network.wifidirect;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.njucs.main.R;

/**
 * Created by mio on 3/27/16.
 */
public class ChooseConsistencyActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.choose_consistency);


    }

    public void chooseConsistency(View view) {
        boolean checked = ((RadioButton) view).isChecked();
        String consistency = null;

        switch (view.getId()) {
            case R.id.radio_weak:
                if (checked) {
                    consistency = getResources().getString(R.string.weak_consistency);

                }
                break;
            case R.id.radio_atomic:
                if (checked) {
                    consistency = getResources().getString(R.string.atomic_consistency);

                }
                break;
        }
        Intent returnIntent = new Intent();
        returnIntent.putExtra(getResources().getString(R.string.consistency), consistency);
        setResult(RESULT_OK, returnIntent);
        finish();

    }
}
