package au.com.sensis;

import java.util.ArrayList;
import java.util.List;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import au.com.sensis.sapi.client.SapiClient;
import au.com.sensis.sapi.client.SapiEnvironment;
import au.com.sensis.sapi.requestmodel.SearchParams;
import au.com.sensis.sapi.responsemodel.Listing;
import au.com.sensis.sapi.responsemodel.SearchResponse;

/**
 * This is a simple app that demonstrates how to use the SAPI Java Client with an AsyncTask.
 * The user provides a query and a location. SAPI is queried with these terms, then the results are displayed in a ListView.
 * Note: before this will work, you need to specify your SAPI API Key and any proxy information in strings.xml. See below.
 * 
 * This project has a dependency on the SAPI Client, available from https://github.com/ShardPhoenix/SapiClient
 *
 */
public class Serp extends ListActivity implements OnScrollListener {
	
	private ListingSerpAdapter adapter;
	private String whatText;
	private String whereText;
	private SapiClient client;
	
	// Need to specify <string name="api_key">your-sapi-key-here</string> in strings.xml
	private String apiKey;
	
	/* 
	 * Put the below in strings.xml if needed:
	 * <string name="proxy_path">your.proxy.url.com</string>
	 * <string name="proxy_port">your proxy port (eg 8080)</string>
    */
	private String proxyHost;
	private int proxyPort;
	
	//TODO: show a message for no results
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		apiKey = getString(R.string.api_key);
		client = new SapiClient(apiKey, SapiEnvironment.TEST);
		
		proxyHost = getString(R.string.proxy_path);
		if (proxyHost != null && !proxyHost.equals("") && !proxyHost.matches("\\s*")) {
		    proxyPort = getString(R.string.proxy_port) != null ? Integer.parseInt(getString(R.string.proxy_port)) : null;
		    client.setProxy(proxyHost, proxyPort);
		}
		
		setContentView(R.layout.serp);
		
		Bundle extras = getIntent().getExtras();
		whatText = extras.getString("what");
		whereText = extras.getString("where");
		
		adapter = new ListingSerpAdapter(this, R.layout.serpitem);
		setListAdapter(adapter);
		GetJsonResponseTask task = new GetJsonResponseTask(this, adapter);
		
		SearchParams params = new SearchParams.Builder()
			.withQuery(whatText)
			.withLocation(whereText)
			// Below are some examples of more types of filters that can be added as desired
//			.withPage(2)
//			.withPostcode(6000)
//			.withRows(5)
//			.withSortBy(SortBy.DISTANCE)
//			.withState(State.WA)
//			.withState(State.NT)
//			.withContent(ContentFilter.BUSINESS_LOGO)
//			.withContent(ContentFilter.SHORT_DESCRIPTOR)
			.build();
		task.execute(params);
		
		getListView().setOnScrollListener(this);
	}

	@Override
	public void onScroll(AbsListView view, int firstVisible, int visibleCount, int totalCount) {
		// do nothing
	}

	@Override
	public void onScrollStateChanged(AbsListView arg0, int arg1) {
		// do nothing
	}
	
	class GetJsonResponseTask extends AsyncTask<SearchParams, Integer, List<Listing>> {
		Context context;
		ProgressDialog waitSpinner;
		private ListingSerpAdapter adapter;
		
		public GetJsonResponseTask(Context context, ListingSerpAdapter adapter) {
			this.context = context;
			this.waitSpinner = new ProgressDialog(context);
			this.adapter = adapter;
		}
		
		@Override
		protected void onPreExecute() {
			waitSpinner = ProgressDialog.show(context, "Searching...", "", true);
		}
		
		@Override
		protected List<Listing> doInBackground(SearchParams... params) {
			try {
				SearchResponse response = client.search(params[0]);
				return response.getResults();
			} catch (Exception e) {
				//TODO: handle errors better?
				Log.e("Error", e.getMessage(), e);
				e.printStackTrace();
				
				return new ArrayList<Listing>();
			}
		}

		@Override
		protected void onPostExecute(List<Listing> results) {
			adapter.addListings(results);
			waitSpinner.cancel();
		}
	}
	
	class ListingSerpAdapter extends ArrayAdapter<Listing> {

		private Context context;
		int resource;
		
		public ListingSerpAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
			this.context = context;
			this.resource = textViewResourceId;
		}
		
		public void addListings(List<Listing> results) {
			for (Listing listing : results) {
				add(listing);
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			
			LinearLayout view;
			if (convertView == null) {
				view = new LinearLayout(context);
				LayoutInflater vi = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				vi.inflate(resource, view, true);
			} else {
				view = (LinearLayout) convertView;
			}
			
			Listing listing = getItem(position);
			
			//TODO: abstract this
			TextView listingName = (TextView) view.findViewById(R.id.listingname);
			TextView listingAddress = (TextView) view.findViewById(R.id.listingaddress);
			TextView listingContact = (TextView) view.findViewById(R.id.listingcontact);
			
			listingName.setText(listing.getName());
			if (listing.getPrimaryAddress() != null) {
				String addressLine = listing.getPrimaryAddress().getAddressLine();
				listingAddress.setText(
						  (addressLine != null ? addressLine + " " : "") 
						+ listing.getPrimaryAddress().getSuburb());
			} else {
				listingAddress.setHeight(0); //TODO: better way to do this?
			}
			if (listing.getPrimaryContacts().get(0) != null) {
				listingContact.setText(listing.getPrimaryContacts().get(0).getValue());
				Linkify.addLinks(listingContact, Linkify.ALL);
			} else {
				listingContact.setHeight(0);
			}
			
			view.setTag(listing); // Store the listing for onListItemClick
			
			return view;
		}
	}
}
