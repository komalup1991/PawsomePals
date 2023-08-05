package edu.northeastern.pawsomepals.ui.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import edu.northeastern.pawsomepals.R;
import edu.northeastern.pawsomepals.models.GroupChatModel;
import edu.northeastern.pawsomepals.models.Users;

public class EditChatRoomInfoActivity extends AppCompatActivity {
    private GroupChatModel groupChatModel;
    private LinearLayout groupNameLayout, groupNoticeLayout;
    private List<Users> groupUsers;
    private ImageButton backBtn;
    private TextView groupName, groupNotice, membersNames;
    private EditText editTextField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_chat_room_info);

        groupUsers = new ArrayList<>();
        initialView();
        groupChatModel = ChatFirebaseUtil.getGroupChatModelFromIntent(getIntent());
        groupName.setText(groupChatModel.getGroupName());

        getGroupUsers();
        membersNames.setText(groupChatModel.getGroupName());
//        membersNames.setText(createMembersDetails());
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        groupNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createDialog();
            }
        });

    }

    private String createMembersDetails() {
        StringBuilder builder = new StringBuilder();
        for (Users user : groupUsers) {
            Log.i("info", user.getName());
            builder.append(user.getName());
            builder.append("  ");
        }
        return builder.toString();
    }

    private void initialView() {
        groupName = findViewById(R.id.group_name_text);
        groupNotice = findViewById(R.id.group_notice_text);
        membersNames = findViewById(R.id.group_members_text);
        backBtn = findViewById(R.id.info_back_button);
        groupNameLayout = findViewById(R.id.groupNameLayout);
        groupNoticeLayout = findViewById(R.id.groupNoticeLayout);
    }

    private void getGroupUsers() {
        List<DocumentReference> references = ChatFirebaseUtil.getGroupFromChatRoom(groupChatModel.getGroupMembers());
        for (DocumentReference reference : references) {
            reference.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                @Override
                public void onSuccess(DocumentSnapshot snapshot) {
                    Users user = snapshot.toObject(Users.class);
                    if (user != null)
                        groupUsers.add(user);
                }
            });
        }
    }

    private void createDialog() {
        editTextField = new EditText(this.getApplicationContext());

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Edit Group Name")
                .setView(editTextField)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String editTextInput = editTextField.getText().toString();
                        Log.d("onclick", "editext value is: " + editTextInput);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create();
        dialog.show();
    }
}