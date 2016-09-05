package james.com.simplezhihudaily.View;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

import james.com.simplezhihudaily.Model.CommentAdapter;
import james.com.simplezhihudaily.R;

import static android.R.attr.id;

public class CommentActivity extends Activity {
    private String idOfArticle;
    private CommentAdapter adapter;
    private TextView sumOfComments;
    private TextView numberOfLongComments;
    private TextView numberOfShortComments;
    private ListView shortCommentListView;
    private ListView longCommentListView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_comment);
        initWidget();
    }
    private void initWidget(){
        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("id");
        idOfArticle = bundle.getString("idOfArticle");
    }
}
