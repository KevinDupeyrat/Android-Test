package iut.paci.classroomcomunnity.frame;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.Response;
import java.util.List;

import iut.paci.classroomcomunnity.R;
import iut.paci.classroomcomunnity.activity.JsonTools;
import iut.paci.classroomcomunnity.activity.MainActivity;
import iut.paci.classroomcomunnity.activity.QuizActivity;
import iut.paci.classroomcomunnity.adapter.FriendAdapter;
import iut.paci.classroomcomunnity.frame.ScanFragment;
import iut.paci.classroomcomunnity.bean.Amis;

/**
 * Fragment des amies qui va gérer
 * la liste des amis présent sur le server
 */
public class FriendFragment extends Fragment {


    private View rootView;
    private ProgressDialog progressDialog;
    private String jSonFriend = "";


    public FriendFragment() {}

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        // Modification du titre
        getActivity().setTitle("Mes Amis");
        // On récupère l'instance de l'activité qui
        // contient ce Fragement
        rootView = inflater.inflate(R.layout.fragment_friend, container, false);

        this.getRemoteFriend();

        return rootView;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    /**
     * Méthode qui permet d'aller chercher la liste
     * d'amis sur le server
     */
    private void getRemoteFriend() {

        // Initialisation de la bar de progression
        this.progressDialog = new ProgressDialog(getContext());
        this.progressDialog.setMessage("Merci de patienter, vos amis arrivent . . .");
        this.progressDialog.setIndeterminate(true);
        this.progressDialog.setCancelable(false);
        this.progressDialog.show();

        // Objet Ion permet comme en Ajax de récupérer une
        // reponse d'un server via HTTP.
        // Ici nous voulons récupérer un fichier Json
        Ion.with(getContext())
                //paci.iut.1235
                .load("http://192.168.137.1/classroom_server/getFriends.php?key=" + MainActivity.getServerCode())
                .asString()
                .withResponse() // Gestion des reponses
                .setCallback(new FutureCallback<Response<String>>() {
                    @Override
                    public void onCompleted(Exception e, Response<String> response) {

                        // Une fois la requête terminé nous
                        // fermons la bar de progression
                        progressDialog.dismiss();

                        // Si la réponse est null
                        if(response == null){
                            Toast.makeText(getContext(), "Erreur: Le serveur ne repond pas !", Toast.LENGTH_SHORT).show();

                        } else {

                            // On verrifi si la requête nous a renvoyer une erreur.
                            // Si elle renvoie False c'est qu'il n'y as pas d'erreur
                            // et on peut passer à la suite
                            if(errorManager(response)){
                                jSonFriend = response.getResult();
                                initListView();
                            }
                        }

                    }
                });

        Log.i("Resultat en String", this.jSonFriend);
    }

    /**
     * Méthode de gestion des erreurs
     * avec le server
     */
    private boolean errorManager(Response<String> response) {

        Log.i("Resultat", response.getResult());

        // Gestion des différents code d'erreur en
        // retour de la requête
        switch (response.getHeaders().code()) {
            case 404:
                Toast.makeText(getContext(), "Erreur 404 page not found !", Toast.LENGTH_SHORT).show();
                return false;
            case 403:
                Toast.makeText(getContext(), "Erreur 404 page not found !", Toast.LENGTH_SHORT).show();
                return false;
        }

        // Si la clés n'est pas bonne
        if(response.getResult().equals("{\"error\" : \"Wrong key !!!\"}")) {

            // On insert le fragment qui va remplacer celui de base
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.contentFrame, new ScanFragment()).commit();

            return false;
        }

        return true;

    }

    /**
     * Méthode de création de la ListView
     */
    private void initListView() {

        // On récupère la listeView
        ListView listView = (ListView) rootView.findViewById(R.id.listView);

        // On utilise la class JsonTools pour récupérer la
        // liste d'amis à partir de la String Json
        List<Amis> amisList = JsonTools.getAmis(this.jSonFriend);

        // On crée l'adapter pour la listeView
        FriendAdapter adapter = new FriendAdapter(rootView.getContext(), R.layout.item_player, amisList);

        // On crée un listener sur chaque bouton
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                Amis ami = (Amis) adapterView.getItemAtPosition(position);

                // On vérifi que l'utilistateur est connecté
                // S'il ne l'ai pas on affiche un message à l'utilisateur
                if(ami.isPresent() == 0){

                    Toast.makeText(getContext(), "Désolé mais " + ami.getNom() + " "
                            + ami.getPrenom() + " n'est pas connecté :(", Toast.LENGTH_SHORT).show();
                } else {

                    goToQuiz(ami);

                }
            }
        });

        // Nous ajoutons notre adapter à notre listView
        listView.setAdapter(adapter);
    }

    /**
     * Méthode qui permet de lancer
     * le Quiz avec l'ami séléctionné
     * @param ami
     */
    private void goToQuiz(Amis ami){

        // Nous récupérons toutes les info que
        // nous avons besoin pour la suite
        String nom = ami.getNom();
        String prenom = ami.getPrenom();
        int isPresent  = ami.isPresent();
        int lastScore = ami.getLastScore();


        // Création d'un Intent (activité)
        Intent intent = new Intent(getContext(), QuizActivity.class);
        // Création d'une boite
        Bundle bundle = new Bundle();
        // Ajout de l'identifiant dans notre boite
        bundle.putString("nom",nom);
        bundle.putString("prenom",prenom);
        bundle.putInt("isPresent",isPresent);
        bundle.putInt("lastScore",lastScore);

        // Ajout de notre boite dans notre prochaine activité
        intent.putExtras(bundle);
        // On demarre une activité
        startActivity(intent);

    }
}
