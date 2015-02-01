package com.zode64.trellodoing;

import android.app.Activity;
// import android.support.v7.app.ActionBarActivity;

import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;

import java.net.URL;
import java.util.List;

public class SpreadsheetActivity extends Activity {

    String worksheetTitle = null;
    LinearLayout layoutForExcel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spreadsheet);

        layoutForExcel = (LinearLayout) findViewById(R.id.excel_layout);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            String USERNAME = "john@zode64.com";
            String PASSWORD = "ujphwatvqfucsbdo";

            SpreadsheetService service =
                    new SpreadsheetService("Testing");
            service.setUserCredentials(USERNAME, PASSWORD);
            // TODO: Authorize the service object for a specific user (see other sections)

            // Define the URL to request.  This should never change.
            URL SPREADSHEET_FEED_URL = new URL(
                    "https://spreadsheets.google.com/feeds/spreadsheets/private/full");

            // Make a request to the API and get all spreadsheets.
            SpreadsheetFeed feed = service.getFeed(SPREADSHEET_FEED_URL,
                    SpreadsheetFeed.class);
            List<SpreadsheetEntry> spreadsheets = feed.getEntries();

            if (spreadsheets.size() == 0) {
                // TODO: There were no spreadsheets, act accordingly.
            }

            // TODO: Choose a spreadsheet more intelligently based on your
            // app's needs.
            SpreadsheetEntry spreadsheet = spreadsheets.get(0);
            System.out.println(spreadsheet.getTitle().getPlainText());
            Log.d("Spread Sheet Title", "" + spreadsheet.getTitle().getPlainText());

            // Make a request to the API to fetch information about all
            // worksheets in the spreadsheet.
            List<WorksheetEntry> worksheets = spreadsheet.getWorksheets();

            // Iterate through each worksheet in the spreadsheet.
            for (WorksheetEntry worksheet : worksheets) {
                // Get the worksheet's title, row count, and column count.
                String title = worksheet.getTitle().getPlainText();
                int rowCount = worksheet.getRowCount();
                int colCount = worksheet.getColCount();

                // Print the fetched information to the screen for this worksheet.
                System.out.println('\t' + title + "- rows:" + rowCount + " cols: " + colCount);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}