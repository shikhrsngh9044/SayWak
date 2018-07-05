package trident.dev.saywak

import android.app.AlertDialog
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.login_custom_dialog.view.*
import kotlinx.android.synthetic.main.register_custom_dialog.view.*

class Login : AppCompatActivity() {

    lateinit var mGoogleSignInClient: GoogleSignInClient
    private val RC_SIGN_IN = 1
    lateinit var mAuth: FirebaseAuth
    lateinit var gso: GoogleSignInOptions
    var back_press: Boolean = false
    lateinit var mAuthStateListener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)


        mAuth = FirebaseAuth.getInstance()

        mAuthStateListener = FirebaseAuth.AuthStateListener {
            if (it.currentUser != null) {
                startActivity(Intent(applicationContext, MainActivity::class.java))
                finish()
            }
        }

        gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)


        googleLogin.setOnClickListener {
            signIn()
        }

        emailLogin.setOnClickListener {

            val dialogBuilder = AlertDialog.Builder(this@Login)
            val view = layoutInflater.inflate(R.layout.register_custom_dialog, null)
            dialogBuilder.setView(view)
            val alertDialog = dialogBuilder.create()
            alertDialog.show()


            val dialogBuilder1 = AlertDialog.Builder(this@Login)
            val view1 = layoutInflater.inflate(R.layout.login_custom_dialog, null)
            dialogBuilder1.setView(view)
            val alertDialog1 = dialogBuilder1.create()

            view.regBtn.setOnClickListener {
                //regDetail()

                val name = view.editFullName.text.toString().trim()
                val email = view.editEmail.text.toString().trim()
                val pass = view.editPass.text.toString().trim()
                val confpass = view.editPassConfirm.text.toString().trim()


                if (TextUtils.isEmpty(name)) {
                    view.editFullName.error = "Field Required!"
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(email)) {
                    view.editEmail.error = "Field Required!"
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(pass)) {
                    view.editPass.error = "Field Required!"
                    return@setOnClickListener
                }
                if (TextUtils.isEmpty(confpass)) {
                    view.editPassConfirm.error = "Field Required!"
                    return@setOnClickListener
                }


                createUser(name, email, pass)
                alertDialog.dismiss()
            }
            view.textL.setOnClickListener {
                alertDialog.setContentView(view1)


                view1.buttonLogin.setOnClickListener {
                    val email1 = view1.editTextEmail.text.toString().trim()
                    val pass1 = view1.editTextPass.text.toString().trim()

                    if (TextUtils.isEmpty(email1)) {
                        view1.editTextEmail.error = "Field Required!"
                        return@setOnClickListener
                    }
                    if (TextUtils.isEmpty(pass1)) {
                        view1.editTextPass.error = "Field Required!"
                        return@setOnClickListener
                    }

                    loginUser(email1, pass1)
                    alertDialog1.dismiss()

                }


            }


        }
    }


    override fun onBackPressed() {
        if (back_press) {
            val a = Intent(Intent.ACTION_MAIN)
            a.addCategory(Intent.CATEGORY_HOME)
            a.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(a)
            return
        }
        this.back_press = true
        Toast.makeText(baseContext, "Press once again to exit!", Toast.LENGTH_SHORT).show()

    }

    override fun onResume() {
        super.onResume()
        this.back_press = false
    }


    private fun loginUser(email: String, pass: String) {


        mAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {

                val currentUser = mAuth.currentUser
                val uid = currentUser!!.uid


                startActivity(Intent(applicationContext, MainActivity::class.java))

            } else {

                Toast.makeText(this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()

            }

        }
    }

    private fun createUser(name: String, email: String, pass: String) {


        mAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(this) { task: Task<AuthResult> ->
            if (task.isSuccessful) {


                val currentUser = mAuth.currentUser
                val uid = currentUser!!.uid

                Toast.makeText(this@Login, "Authentication Success!!", Toast.LENGTH_SHORT).show()

            } else {
                Toast.makeText(this@Login, "Authentication Failed!!", Toast.LENGTH_SHORT).show()

            }
            // ...
        }
    }

    override fun onStart() {
        super.onStart()
        mAuth.addAuthStateListener(mAuthStateListener)
    }

    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account)
//we can use Update Ui
            } catch (e: ApiException) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun firebaseAuthWithGoogle(accout: GoogleSignInAccount) {

        val credential = GoogleAuthProvider.getCredential(accout.idToken, null)
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, OnCompleteListener<AuthResult> { task ->
                    if (task.isSuccessful) {

                        /* Picasso.get().load(accout.photoUrl.toString()).networkPolicy(NetworkPolicy.OFFLINE).into(image)

                         val m = ProfileModel(1, "${accout.photoUrl.toString()}", "${accout.displayName.toString()}", "${accout.email.toString()}")
                         profileHelper.updateLink(m) */


                        val currentUser = mAuth.currentUser
                        val uid = currentUser!!.uid


                        Toast.makeText(this, "Google sign Success", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(applicationContext, MainActivity::class.java))
                    } else {

                        Toast.makeText(this, "Google sign in failed", Toast.LENGTH_SHORT).show()
                    }

                })
    }
}
