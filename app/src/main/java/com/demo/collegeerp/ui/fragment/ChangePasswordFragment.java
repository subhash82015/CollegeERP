package com.demo.collegeerp.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.demo.collegeerp.databinding.FragmentChangePasswordBinding;
import com.demo.collegeerp.ui.activity.CommonActivity;
import com.demo.collegeerp.ui.activity.DashboardActivity;
import com.demo.collegeerp.ui.activity.LoginActivity;
import com.demo.collegeerp.utils.Constants;
import com.demo.collegeerp.utils.CustomProgressDialog;
import com.demo.collegeerp.utils.FirebaseRepo;
import com.demo.collegeerp.utils.SharedPreferenceUtil;
import com.demo.collegeerp.utils.Tools;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;


public class ChangePasswordFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }

    private String TAG = "AddFeesFragment";

    FragmentChangePasswordBinding binding;
    private FirebaseFirestore firebaseFirestore;
    CustomProgressDialog customProgressDialog;
    String currentPassword = "", newPassword = "", confirmPassword = "";
    private SharedPreferenceUtil sharedPreferenceUtil;

    public static AddFeesFragment newInstance(String param1, String param2) {
        AddFeesFragment fragment = new AddFeesFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentChangePasswordBinding.inflate(getLayoutInflater(), container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        firebaseFirestore = FirebaseRepo.createInstance();
        sharedPreferenceUtil = SharedPreferenceUtil.getInstance(requireActivity());
        customProgressDialog = new CustomProgressDialog(requireActivity(), "Please wait....");
        handleClickListener();
    }

    private void handleClickListener() {
        binding.btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkValidation();
            }
        });
    }


    private void checkValidation() {
        currentPassword = binding.etCurrentPassword.getText().toString();
        newPassword = binding.etNewPassword.getText().toString();
        confirmPassword = binding.etConfirmPassword.getText().toString();
        if (currentPassword.equals("")) {
            Tools.showToast(requireActivity(), "Please enter current password");
        } else if (newPassword.equals("")) {
            Tools.showToast(requireActivity(), "Please enter new password");
        } else if (confirmPassword.equals("")) {
            Tools.showToast(requireActivity(), "please enter confirm password");
        } else if (!newPassword.equals(confirmPassword)) {
            Tools.showToast(requireActivity(), "Confirm and New password should be same");
        } else {
            String regexPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$";

            if (newPassword.matches(regexPattern)) {
                checkPassword();
            } else {
                // Password is not strong
                Tools.showToast(requireActivity(), "Password is not strong");

            }
        }
    }

    public void checkPassword() {
        customProgressDialog.show();
        CollectionReference usersRef = firebaseFirestore.collection(Constants.ACCOUNT_COLLECTION_NAME);
        Query query = usersRef.whereEqualTo("mobile", sharedPreferenceUtil.getUserDetails(Constants.MOBILE)).whereEqualTo("password", currentPassword);
        query.get().addOnCompleteListener(task -> {
            customProgressDialog.dismiss();
            if (task.isSuccessful()) {
                QuerySnapshot querySnapshot = task.getResult();
                if (querySnapshot != null && !querySnapshot.isEmpty()) {
                    for (QueryDocumentSnapshot document : querySnapshot) {
                        updatePassword();
                    }
                } else {
                    Tools.showToast(requireActivity(), "Current password is not valid");
                }
            } else {
                Tools.logs(TAG, "Error " + task.getException());
            }
        });
    }


    private void updatePassword() {
        // Get a reference to the document you want to update
        DocumentReference docRef = firebaseFirestore.collection(Constants.ACCOUNT_COLLECTION_NAME).document(sharedPreferenceUtil.getUserDetails(Constants.MOBILE));

        Map<String, Object> updates = new HashMap<>();
        updates.put("password", String.valueOf(newPassword));
        docRef.update(updates)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Updated successfully
                        Tools.showToast(requireActivity(), "Password Updated");
                        logout();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Tools.showToast(requireActivity(), "Error while Password Updation");
                        // Handle any errors
                    }
                });
    }


    private void logout() {
        sharedPreferenceUtil.setUserDetails(String.valueOf(Constants.USER_TYPE), "");
        sharedPreferenceUtil.setUserId(0L);
        sharedPreferenceUtil.setLoginAlready(false);
        Intent intent = new Intent(requireActivity(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        if (getActivity() instanceof CommonActivity) {
            CommonActivity commonActivity = (CommonActivity) getActivity();
            commonActivity.finishAffinity();
        } else {
            // Handle the case where the hosting activity is not an instance of CommonActivity
        }

    }

}
