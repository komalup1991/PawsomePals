package edu.northeastern.pawsomepals.adapters;

//public class ChatUserRecyclerAdapter extends FirestoreRecyclerAdapter<UserModel,ChatUserRecyclerAdapter.UserModelViewHolder> {
//
//    Context context;
//
//    public ChatUserRecyclerAdapter(@NonNull FirestoreRecyclerOptions<UserModel> options, Context context) {
//        super(options);
//        this.context = context;
//    }
//
//    @Override
//    protected void onBindViewHolder(@NonNull UserModelViewHolder holder, int position, @NonNull UserModel model) {
//        holder.userNameText.setText(model.getUserName());
//        //set up image
//
//        //Check if the result is "me"
//        if (model.getUserId().equals(ChatFirebaseUtil.currentUserId())){
//            model.usernameText.setText(model.getUserName()+"(Me)");
//        }
//        //Listener to open new ChatRoomActivity
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                //navigate to chat activity;
//                Intent intent = new Intent(context, ChatRoomActivity.class);
//                ChatFirebaseUtil.passUserModelAsIntent(intent,model);
//                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                context.startActivity(intent);
//            }
//        });
//    }
//
//    @NonNull
//    @Override
//    public UserModelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(context).inflate(R.layout.chat_search_item,parent,false);
//        return new UserModelViewHolder(view);
//    }
//
//    class UserModelViewHolder extends RecyclerView.ViewHolder{
//        TextView userNameText;
//        ImageView profilePic;
//
//        public UserModelViewHolder(@NonNull View itemView) {
//            super(itemView);
//            userNameText = itemView.findViewById(R.id.chat_user_name_text);
//            profilePic = itemView.findViewById(R.id.chat_profile_pic);
//        }
//    }
//}
