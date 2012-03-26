package au.com.sensis;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class Search extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		Button searchButton = (Button) findViewById(R.id.searchbutton);
		final Context context = this;
		searchButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				EditText whatText = (EditText) findViewById(R.id.whattext);
				EditText whereText = (EditText) findViewById(R.id.wheretext);

				Intent getResults = new Intent(context, Serp.class);
				getResults.putExtra("what", whatText.getText().toString());
				getResults.putExtra("where", whereText.getText().toString());
				startActivity(getResults);
			}
		});
	}
}