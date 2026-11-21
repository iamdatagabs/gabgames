package br.com.gabgames.medicina

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.random.Random

private val android.content.Context.dataStore by preferencesDataStore(name = "tier_list")
private val subjects = listOf(
    "Anatomia Humana",
    "Histologia",
    "Embriologia",
    "Biologia Celular e Molecular",
    "Bioquímica",
    "Genética Médica",
    "Fisiologia Humana",
    "Imunologia",
    "Microbiologia",
    "Parasitologia",
    "Patologia Geral",
    "Farmacologia",
    "Semiologia Médica",
    "Semiotécnica",
    "Ética Médica e Bioética",
    "Saúde Coletiva",
    "Epidemiologia",
    "Medicina de Família e Comunidade",
    "Medicina Preventiva",
    "Medicina Baseada em Evidências",
    "Habilidades Médicas",
    "Comunicação e Relação Médico-Paciente",
    "Biossegurança",
    "Diagnóstico por Imagem",
    "Medicina Laboratorial",
    "Clínica Médica",
    "Cardiologia",
    "Pneumologia",
    "Gastroenterologia",
    "Nefrologia",
    "Endocrinologia",
    "Hematologia",
    "Reumatologia",
    "Neurologia",
    "Dermatologia",
    "Infectologia",
    "Oncologia",
    "Psiquiatria",
    "Pediatria",
    "Neonatologia",
    "Ginecologia",
    "Obstetrícia",
    "Cirurgia Geral",
    "Anestesiologia",
    "Ortopedia e Traumatologia",
    "Oftalmologia",
    "Otorrinolaringologia",
    "Urologia",
    "Medicina de Urgência e Emergência",
    "Terapia Intensiva",
    "Medicina Legal e Perícia Médica",
    "Internato"
)
private data class Tier(val key:String,val title:String,val color:Color)
private val tiers = listOf(
    Tier("S","Melhor de todas",Color(0xFFFF777E)),
    Tier("A","Muito boa",Color(0xFFFFB66D)),
    Tier("B","Boa",Color(0xFFF1D36B)),
    Tier("C","Tem que fazer, né?",Color(0xFFA6D78A)),
    Tier("D","Pago pra alguém fazer",Color(0xFF82C4DC)),
    Tier("E","Gostei tanto que fiz 2x",Color(0xFFB5A0E8))
)
private val bg=Color(0xFF10111A)
private val panel=Color(0xFF191B28)
private val card=Color(0xFF282C40)
private val accent=Color(0xFF83D8C6)

class MainActivity:ComponentActivity(){
    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);window.statusBarColor=android.graphics.Color.rgb(16,17,26);window.navigationBarColor=android.graphics.Color.rgb(16,17,26);setContent { MaterialTheme(colorScheme=darkColorScheme(background=bg,surface=panel,primary=accent)) { MedicineApp() } }}
}

@Composable
private fun MedicineApp(){
    val context= LocalContext.current
    val scope= rememberCoroutineScope()
    val stateKey= remember { stringPreferencesKey("classification") }
    val assignments= remember { mutableStateMapOf<String,String>() }
    var loaded by remember { mutableStateOf(false) }
    var subjectOrder by remember { mutableStateOf(subjects) }
    var dragging by remember { mutableStateOf<String?>(null) }
    var dragDelta by remember { mutableStateOf(Offset.Zero) }
    var dragOrigin by remember { mutableStateOf(Offset.Zero) }
    var dragSize by remember { mutableStateOf(IntSize.Zero) }
    val zoneBounds= remember { mutableStateMapOf<String,Rect>() }
    var message by remember { mutableStateOf("Toque e segure uma disciplina para arrastar") }
    LaunchedEffect(Unit){
        val raw=context.dataStore.data.first()[stateKey]
        if(!raw.isNullOrBlank())try{val json=JSONObject(raw);subjects.forEach { n->if(json.has(n))assignments[n]=json.getString(n)} }catch(_:Exception){}
        loaded=true
    }
    fun persist(){scope.launch{val json=JSONObject();assignments.forEach{(k,v)->json.put(k,v)};context.dataStore.edit{it[stateKey]=json.toString()}}}
    fun drop(){val name=dragging?:return;val point=dragOrigin+dragDelta+Offset(dragSize.width/2f,dragSize.height/2f);val target=zoneBounds.entries.firstOrNull{it.value.contains(point)}?.key
        if(target!=null){if(target=="POOL")assignments.remove(name) else assignments[name]=target;persist();message="Disciplina classificada em $target"}
        dragging=null;dragDelta=Offset.Zero
    }
    val remaining=subjectOrder.filter{it !in assignments}
    Scaffold(containerColor=bg, snackbarHost={}) { padding->
        Column(Modifier.fillMaxSize().padding(padding).padding(horizontal=16.dp)) {
            Row(Modifier.fillMaxWidth().padding(top=18.dp,bottom=8.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically){
                Text("GABGAMES",color=accent,fontWeight=FontWeight.ExtraBold,style=MaterialTheme.typography.labelLarge)
                Text("MEDICINA • TIER LIST",color=Color.LightGray,style=MaterialTheme.typography.labelSmall)
            }
            Text("Medicina.",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Black,color=Color.White)
            Text("Tier List.",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Black,color=accent)
            Text(message,color=Color.LightGray,style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=5.dp,bottom=12.dp))
            Row(horizontalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.fillMaxWidth()){
                OutlinedButton(onClick={subjectOrder=subjectOrder.shuffled();message="Disciplinas embaralhadas!"} ,modifier=Modifier.weight(1f)){Text("Embaralhar")}
                OutlinedButton(onClick={assignments.clear();persist();message="Tier list reiniciada"},modifier=Modifier.weight(1f)){Text("Reiniciar")}
            }
            Text("${assignments.size} de ${subjects.size} classificadas",color=accent,fontWeight=FontWeight.Bold,modifier=Modifier.padding(vertical=9.dp))
            LazyColumn(verticalArrangement=Arrangement.spacedBy(8.dp),contentPadding=PaddingValues(bottom=20.dp),modifier=Modifier.weight(1f)){
                items(tiers,key={it.key}){tier->
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(panel).border(1.dp,Color(0xFF30344A),RoundedCornerShape(14.dp)).onGloballyPositioned{zoneBounds[tier.key]=it.boundsInRoot()}){
                        Row(verticalAlignment=Alignment.CenterVertically,modifier=Modifier.fillMaxWidth().background(tier.color).padding(horizontal=13.dp,vertical=8.dp)){
                            Text(tier.key,color=Color(0xFF17131A),fontWeight=FontWeight.Black,style=MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.width(12.dp));Text(tier.title,color=Color(0xFF17131A),fontWeight=FontWeight.Bold,style=MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.weight(1f));Text(assignments.count{it.value==tier.key}.toString(),color=Color(0xFF17131A),fontWeight=FontWeight.Bold)
                        }
                        FlowRowCompat(items=subjectOrder.filter{assignments[it]==tier.key},dragging=dragging,dragDelta=dragDelta,onStart={name,origin,size->dragging=name;dragOrigin=origin;dragSize=size;dragDelta=Offset.Zero},onMove={dragDelta=it},onEnd={drop()})
                    }
                }
                item{
                    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp)).background(panel).border(1.dp,Color(0xFF30344A),RoundedCornerShape(14.dp)).onGloballyPositioned{zoneBounds["POOL"]=it.boundsInRoot()}.padding(12.dp)){
                        Text("🩺 Matérias para classificar",fontWeight=FontWeight.Bold,color=Color.White)
                        Spacer(Modifier.height(8.dp))
                        FlowRowCompat(items=remaining,dragging=dragging,dragDelta=dragDelta,onStart={name,origin,size->dragging=name;dragOrigin=origin;dragSize=size;dragDelta=Offset.Zero},onMove={dragDelta=it},onEnd={drop()})
                    }
                }
            }
        }
    }
    if(!loaded)Box(Modifier.fillMaxSize().background(bg),contentAlignment=Alignment.Center){CircularProgressIndicator(color=accent)}
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun FlowRowCompat(items:List<String>,dragging:String?,dragDelta:Offset,onStart:(String,Offset,IntSize)->Unit,onMove:(Offset)->Unit,onEnd:()->Unit){
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement=Arrangement.spacedBy(7.dp),verticalArrangement=Arrangement.spacedBy(7.dp),modifier=Modifier.fillMaxWidth().padding(9.dp)){
        items.forEach { name->
            val active=dragging==name
            val alpha by animateFloatAsState(if(active)0.28f else 1f,label="dragAlpha")
            var origin by remember(name){mutableStateOf(Offset.Zero)}
            var size by remember(name){mutableStateOf(IntSize.Zero)}
            Surface(color=card,shape=RoundedCornerShape(9.dp),modifier=Modifier.graphicsLayer{this.alpha=alpha; if(active){translationX=dragDelta.x;translationY=dragDelta.y;scaleX=1.04f;scaleY=1.04f}}.onGloballyPositioned{origin=it.positionInRoot();size=it.size}.pointerInput(name){
                detectDragGesturesAfterLongPress(onDragStart={onStart(name,origin,size)},onDrag={change,amount->change.consume();onMove(dragDelta+amount)},onDragEnd=onEnd,onDragCancel=onEnd)
            }.border(1.dp,Color(0xFF414761),RoundedCornerShape(9.dp))){
                Text(name,color=Color.White,fontWeight=FontWeight.SemiBold,style=MaterialTheme.typography.labelMedium,modifier=Modifier.padding(horizontal=11.dp,vertical=10.dp))
            }
        }
    }
}
