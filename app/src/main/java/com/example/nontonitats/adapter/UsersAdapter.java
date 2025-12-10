package com.example.nontonitats.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.nontonitats.R;
import com.example.nontonitats.response.UserData;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> {

    private List<UserData> userList;

    public UsersAdapter(List<UserData> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        UserData user = userList.get(position);

        holder.txtName.setText(user.getName());
        holder.txtEmail.setText(user.getEmail());
        holder.txtRole.setText("Role: " + user.getRole());
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {

        TextView txtName, txtEmail, txtRole;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);

            txtName = itemView.findViewById(R.id.txtUserName);
            txtEmail = itemView.findViewById(R.id.txtUserEmail);
            txtRole = itemView.findViewById(R.id.txtUserRole);
        }
    }
}
