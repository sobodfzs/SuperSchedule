package com.example.SuperSchedule.viewmodel;


import android.app.Application;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.SuperSchedule.entity.Customer;
import com.example.SuperSchedule.repository.CustomerRepository;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class UserViewModel extends AndroidViewModel {
   // private CustomerRepository cRepository;
    @Nullable
    private MutableLiveData<FirebaseUser> user;
    public UserViewModel (Application application) {
        super(application);
        //cRepository = new CustomerRepository(application);
        user=new MutableLiveData<FirebaseUser>();
        update();
    }
    public MutableLiveData<FirebaseUser> getUser() {
        return user;
    }
    public void update() {
        user.setValue(FirebaseAuth.getInstance().getCurrentUser()) ;
    }
    /*@RequiresApi(api = Build.VERSION_CODES.N)
    public CompletableFuture<Customer> findByIDFuture(final int customerId){
        return cRepository.findByIDFuture(customerId);
    }*/
    /*public void insert(Customer customer) {
        cRepository.insert(customer);
    }

    public void deleteAll() {
        cRepository.deleteAll();
    }
      */
}