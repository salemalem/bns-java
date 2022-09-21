package com.example.dannynamecoinappc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.TypefaceSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import de.schildbach.wallet.integration.BitcoinIntegration;


import io.realm.Realm;
import io.realm.mongodb.App;
import io.realm.mongodb.AppConfiguration;
import io.realm.mongodb.Credentials;
import io.realm.mongodb.RealmResultTask;
import io.realm.mongodb.User;
import io.realm.mongodb.mongo.MongoClient;
import io.realm.mongodb.mongo.MongoCollection;
import io.realm.mongodb.mongo.MongoDatabase;

import com.example.namecoin.HistoryModel;

import org.bson.Document;

//import com.mongodb.ConnectionString;
//import com.mongodb.MongoClientSettings;
//import com.mongodb.MongoException;
//import com.mongodb.ServerApi;
//import com.mongodb.ServerApiVersion;
//import com.mongodb.client.MongoClient;
//import com.mongodb.client.MongoClients;
//import com.mongodb.client.MongoCollection;
//import com.mongodb.client.MongoDatabase;

public class MainActivity extends AppCompatActivity {

    private static final long AMOUNT = 500000;
    private static final String[] DONATION_ADDRESSES_MAINNET = { "18CK5k1gajRKKSC7yVSTXT9LUzbheh1XY4", "1PZmMahjbfsTy6DsaRyfStzoWTPppWwDnZ" };
    private static final String[] DONATION_ADDRESSES_TESTNET = { "mkCLjaXncyw8eSWJBcBtnTgviU85z5PfwS", "mwEacn7pYszzxfgcNaVUzYvzL6ypRJzB6A" };
    private static final String MEMO = "Sample donation";
    private static final int REQUEST_CODE = 0;

    private Button donateButton, requestButton;
    private TextView donateMessage;

    private SwitchCompat swBuy;
    private TextView txtManage;
    private RecyclerView rcHistory;
    private RecyclerView.LayoutManager layoutManager;
    private HistoryAdapter adapter;

    MongoDatabase mongoDatabase;
    MongoClient mongoClient;
    TextView txtStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);

        swBuy = findViewById(R.id.swBuy);
        txtManage = findViewById(R.id.txtManage);
        rcHistory = findViewById(R.id.rcHistory);
        txtStatus = findViewById(R.id.txtStatus);
        layoutManager = new LinearLayoutManager(this);
        rcHistory.setLayoutManager(layoutManager);
        adapter = new HistoryAdapter();
        rcHistory.setAdapter(adapter);
        ArrayList<HistoryModel> sampleList =new ArrayList<>();
        sampleList.add(new HistoryModel("dummyolaf.bit","3 hour ago"));
        sampleList.add(new HistoryModel("bitsandwitch.bit","5 days ago"));
        adapter.setData(sampleList);
        Realm.init(this);

        String appID = "namecoin-clpxi";
        App app = new App(new AppConfiguration.Builder(appID)
                .build());

        Credentials credentials = Credentials.anonymous();

        app.loginAsync(credentials, result -> {
            if (result.isSuccess()) {
                Log.v("QUICKSTART", "Successfully authenticated anonymously.");
                User user = app.currentUser();
                // interact with realm using your user object here
                mongoClient = user.getMongoClient("mongodb-atlas");
                mongoDatabase = mongoClient.getDatabase("Namecoin");
            } else {
                Log.e("QUICKSTART", "Failed to log in. Error: " + result.getError());
            }
        });


        donateButton = (Button) findViewById(R.id.btnRegister);
        donateButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(final View v)
            {
                handleDonate();
            }
        });

     /*   requestButton = (Button) findViewById(R.id.sample_request_button);
        requestButton.setOnClickListener(new View.OnClickListener()
        {
            public void onClick(final View v)
            {
        //        handleRequest();
            }
        });

        donateMessage = (TextView) findViewById(R.id.sample_donate_message); */
    }

    private String[] donationAddresses()
    {
        //    final boolean isMainnet = ((RadioButton) findViewById(R.id.sample_network_mainnet)).isChecked();
        final boolean isMainnet = false;

        return isMainnet ? DONATION_ADDRESSES_MAINNET : DONATION_ADDRESSES_TESTNET;
    }

    private void handleDonate()  // register username
    {
        TextView username = this.findViewById(R.id.edtUserName);

        MongoCollection<Document> mongoCollection = mongoDatabase.getCollection("namecoins");
        try {
//                    mongoCollection.insertOne(new Document("userid",1).append("data","data"));
            Long usernameCount = mongoCollection.count(new Document("username", username.getText().toString())).get();

            if(usernameCount == 0) {
                mongoCollection.insertOne(new Document("username", username.getText().toString())).getAsync(callback -> {
                    if (callback.isSuccess()) {
    //                    System.out.println(callback.toString().contains());
                        Log.v("Data:", "Data Inserted Successfully");
                        txtStatus.setText("Registered successfully");
                    } else {
                        Log.v("Data:", "Error:" + callback.getError().toString());
                        txtStatus.setText("Name is already taken");
                    }
                });
            } else {
                txtStatus.setText("Name is already taken");
            }


        } catch (Exception e) {
            System.out.println("Exception for crash" + e);
            txtStatus.setText("Name is already taken");
        }


//        System.out.println("Username:" + username.getText());

        //        final String[] addresses = donationAddresses();
//
//        BitcoinIntegration.requestForResult(MainActivity.this, REQUEST_CODE, addresses[0]);
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                final String txHash = BitcoinIntegration.transactionHashFromResult(data);
                if (txHash != null) {
                    final SpannableStringBuilder messageBuilder = new SpannableStringBuilder("Transaction hash:\n");
                    messageBuilder.append(txHash);
                    messageBuilder.setSpan(new TypefaceSpan("monospace"), messageBuilder.length() - txHash.length(), messageBuilder.length(),
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                    if (BitcoinIntegration.paymentFromResult(data) != null)
                        messageBuilder.append("\n(also a BIP70 payment message was received)");

                    donateMessage.setText(messageBuilder);
                    donateMessage.setVisibility(View.VISIBLE);
                }

                Toast.makeText(this, "Thank you!", Toast.LENGTH_LONG).show();
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Cancelled.", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Unknown result.", Toast.LENGTH_LONG).show();
            }
        }
    }
}