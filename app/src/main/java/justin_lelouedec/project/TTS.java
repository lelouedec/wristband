package justin_lelouedec.project;

import android.content.Intent;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

public class TTS extends AppCompatActivity implements TextToSpeech.OnInitListener{

    EditText editText ;
    Button french;
    Button english;
    TextToSpeech tts;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tts);
        tts = new TextToSpeech(getBaseContext(),this);

        editText = (EditText) findViewById(R.id.editText);

         french = (Button)findViewById(R.id.button);

        french.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = tts.setLanguage(Locale.FRENCH);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported");
                } else {
                    String text = editText.getText().toString();

                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }
            }
        });
         english = (Button)findViewById(R.id.button2);

        english.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int result = tts.setLanguage(Locale.ENGLISH);
                if (result == TextToSpeech.LANG_MISSING_DATA
                        || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "This Language is not supported");
                } else {
                    String text = editText.getText().toString();

                    tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
                }

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_tt, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onInit(int status) {

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        finish();
    }
}
