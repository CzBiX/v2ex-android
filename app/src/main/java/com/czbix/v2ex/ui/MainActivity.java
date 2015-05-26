package com.czbix.v2ex.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.czbix.v2ex.R;
import com.czbix.v2ex.model.Node;
import com.czbix.v2ex.model.Topic;
import com.czbix.v2ex.ui.fragment.TopicListFragment;


public class MainActivity extends AppCompatActivity implements TopicListFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        addFragmentToView();
    }

    private void addFragmentToView() {
        Node node = new Node.Builder().setTitle("Linux").setName("linux").createNode();

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment, TopicListFragment.newInstance(node))
                .commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    public void onFragmentInteraction(Topic topic) {
        Toast.makeText(this, topic.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
