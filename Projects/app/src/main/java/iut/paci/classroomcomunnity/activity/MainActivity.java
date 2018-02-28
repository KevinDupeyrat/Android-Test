package iut.paci.classroomcomunnity.activity;


import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;

import iut.paci.classroomcomunnity.R;
import iut.paci.classroomcomunnity.frame.AboutFragment;
import iut.paci.classroomcomunnity.frame.FriendFragment;
import iut.paci.classroomcomunnity.frame.HomeFragment;
import iut.paci.classroomcomunnity.frame.ScanFragment;
import iut.paci.classroomcomunnity.frame.SettingFragment;

/**
 * Activité pricincipale qui
 * permet de switcher avec les Framgments
 */
public class MainActivity extends AppCompatActivity {

    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private NavigationView nav;
    private static String serverCode;


    /**
     * Setter du code Server
     *
     * @param code
     */
    public static void setServerCode(String code) {
        serverCode = code;
    }

    /**
     * Getter du code server
     *
     * @return
     */
    public static String getServerCode() {
        return serverCode;
    }


    /**
     * Méthode appelé à la création de l'activité
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // On recupère le Drawer
        drawer = (DrawerLayout) findViewById(R.id.drawer);

        // On vérifie que l'utilisateur posséde Android Marshmalow
        // et que la demande de permition pour la camera n'est
        // pas déjà autorisé
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED) {

            // On fait la demande de permition à l'utilisateur
            requestPermissions(new String[]{Manifest.permission.CAMERA},99);

        } else { // Si l'autorisation a déjà était donnée

            this.creatDrawer();
        }
    }


    /**
     * Méthode qui permet de faire une action
     * en fonction du resultat de la demande d'autorisation
     * pour la caméra
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        // Si nous avons l'autorisation
        if(requestCode==99 && grantResults[0]==PackageManager.PERMISSION_GRANTED){

            this.creatDrawer();

        } else {

            // Si la permition n'est pas donné, on bloque le drawer
            // de l'activité pour que l'utilisateur ne puisse rien faire
            drawer.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        }
    }

    private void creatDrawer() {

        // On recupère le Navigateur View
        nav = (NavigationView) findViewById(R.id.nav_view);
        nav.getMenu().performIdentifierAction(R.id.nav_friend, 0);
        nav.bringToFront();

        // Setup drawer view
        setupDrawerContent(nav);

        // Création du bouton pour ouvrir et fermet le Drawer
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.open, R.string.close);

        // On ajoute un listener au Drawer qui écoutera
        // les événement sur le bouton toggle
        drawer.setDrawerListener(toggle);
        // Synchronisation du toggle avec le Drower
        toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        nav.getMenu().performIdentifierAction(R.id.nav_home, 0);
    }

    /**
     * Méthode pour initialiser le
     * contenu du Drawer
     * @param navigationView
     */
    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            selectDrawerItem(menuItem);
                        }
                        return true;
                    }
                });
    }


    /**
     * Méthode pour la gestion de la selection dans le menu
     * @param menuItem
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void selectDrawerItem(MenuItem menuItem) {

        ((FrameLayout)findViewById(R.id.contentFrame)).setBackground(null);

        // Creation d'un nouveau fragment que nous mettrons à la place
        // de celle par défaut
        Fragment fragment = null;
        Class fragmentClass;
        switch(menuItem.getItemId()) {
            case R.id.nav_friend:
                fragmentClass = FriendFragment.class;
                break;
            case R.id.nav_setting:
                fragmentClass = SettingFragment.class;
                break;
            case R.id.nav_about:
                fragmentClass = AboutFragment.class;
                break;
            case R.id.nav_home:
                fragmentClass = HomeFragment.class;
                break;
            case R.id.nav_photo:
                fragmentClass = ScanFragment.class;
                break;
            case R.id.nav_logout:
                this.finish();
                return;
            default:
                fragmentClass = null;
                return;
        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // On insert le fragment qui va remplacer celui de base
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.contentFrame, fragment).commit();

        // Surbrillance dans le Navigateur View de l'item séléctionné
        menuItem.setChecked(true);
        // changement du titre de l'action bar
        setTitle(menuItem.getTitle());
        // Fermeture du Drawer qui contient le Navigateur View
        drawer.closeDrawer(GravityCompat.START);
    }


    /**
     * Méthode qui permet de savoir si le bouton
     * a était activé
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        if(toggle.onOptionsItemSelected(item))
            return true;

        return false;
    }

}
