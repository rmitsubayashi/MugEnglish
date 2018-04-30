package com.linnca.pelicann.lessonscript;

import android.speech.tts.TextToSpeech;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.linnca.pelicann.R;
import com.linnca.pelicann.mainactivity.TextToSpeechHelper;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import pelicann.linnca.com.corefunctionality.lessonscript.Script;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSentence;
import pelicann.linnca.com.corefunctionality.lessonscript.ScriptSpeaker;

public class LessonScriptAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ScriptSentence> sentences;
    private String imageURL;
    private final String SENTENCE_INFO_TAG = "sentenceInfo";
    private final int SENTENCE_INFO_VIEW_TYPE = 1;
    private final String GO_TO_QUESTION_TAG = "goToQuestion";
    private final int GO_TO_QUESTION_VIEW_TYPE = 2;
    private final int SENTENCE_VIEW_TYPE = 3;
    private final String LESSON_NUMBER_TOGGLE_TAG = "lessonNumberToggle";
    private final int LESSON_NUMBER_TOGGLE_VIEW_TYPE = 4;
    private final String IMAGE_TAG = "image";
    private final int IMAGE_TYPE = 5;

    private int sentenceIndexOfCurrentlyShownInfo = -1;
    //int = index
    private Map<ScriptSpeaker, Integer> speakerIndices = new HashMap<>();
    private TextToSpeech textToSpeech;
    private LessonScriptAdapterListener listener;

    private boolean lessonToggleEnabled = true;

    private int lessonNumber;
    private int totalLessonCt;

    interface LessonScriptAdapterListener {
        void toQuestion();
        void toPrev();
        void toNext();
    }

    LessonScriptAdapter(Script script, int lessonNumber, int totalLessonCt, LessonScriptAdapterListener listener, TextToSpeech tts){
        this.lessonNumber = lessonNumber;
        this.totalLessonCt = totalLessonCt;
        this.textToSpeech = tts;
        this.listener = listener;
        this.sentences = new LinkedList<>(script.getSentences());
        this.imageURL = script.getImageURL();
        setSpeakers(this.sentences);
        addNonSentenceRows();
    }

    @Override
    public int getItemCount(){
        return sentences.size();
    }

    @Override
    public int getItemViewType(int position){
        ScriptSentence sentence = sentences.get(position);
        switch (sentence.getSentenceJP()){
            case SENTENCE_INFO_TAG:
                return SENTENCE_INFO_VIEW_TYPE;
            case GO_TO_QUESTION_TAG:
                return GO_TO_QUESTION_VIEW_TYPE;
            case LESSON_NUMBER_TOGGLE_TAG:
                return LESSON_NUMBER_TOGGLE_VIEW_TYPE;
            case IMAGE_TAG:
                return IMAGE_TYPE;
            default:
                return SENTENCE_VIEW_TYPE;
        }
    }

    void updateScript(Script script, int lessonNumber, LessonScriptAdapterListener listener){
        this.sentences = new LinkedList<>(script.getSentences());
        this.imageURL = script.getImageURL();
        this.speakerIndices.clear();
        setSpeakers(this.sentences);
        this.sentenceIndexOfCurrentlyShownInfo = -1;
        this.lessonNumber = lessonNumber;
        this.listener = listener;
        //total lesson ct should remain the same
        addNonSentenceRows();
        this.lessonToggleEnabled = true;

        notifyDataSetChanged();
    }

    private void setSpeakers(List<ScriptSentence> sentences){
        for (ScriptSentence sentence : sentences){
            ScriptSpeaker speaker = sentence.getSpeaker();
            setSpeakerIndex(speaker);
        }
    }

    private void setSpeakerIndex(ScriptSpeaker speaker){
        int speakerIndex;
        if (!speakerIndices.containsKey(speaker)){
            speakerIndex = speakerIndices.size()+1;
            speakerIndices.put(speaker, speakerIndex);
        }
    }

    private void addNonSentenceRows(){
        if (sentences == null){
            sentences = new LinkedList<>();
        }
        ScriptSentence toQuestion = new ScriptSentence();
        toQuestion.setSentence(GO_TO_QUESTION_TAG, GO_TO_QUESTION_TAG);
        sentences.add(toQuestion);

        ScriptSentence lessonNumberToggle = new ScriptSentence();
        lessonNumberToggle.setSentence(LESSON_NUMBER_TOGGLE_TAG, LESSON_NUMBER_TOGGLE_TAG);
        sentences.add(0, lessonNumberToggle);

        ScriptSentence speakerInfo = new ScriptSentence();
        speakerInfo.setSentence(IMAGE_TAG, IMAGE_TAG);
        sentences.add(1, speakerInfo);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView;
        switch(viewType){
            case SENTENCE_INFO_VIEW_TYPE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.inflatable_lesson_script_sentence_extra_info, parent, false);
                return new LessonScriptSentenceExtraInfoViewHolder(itemView);
            case GO_TO_QUESTION_VIEW_TYPE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.inflatable_lesson_script_to_question, parent, false);
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        listener.toQuestion();
                    }
                });
                return new RecyclerView.ViewHolder(itemView){};
            case LESSON_NUMBER_TOGGLE_VIEW_TYPE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.inflatable_lesson_script_lesson_toggle, parent, false);
                return new LessonScriptToggleViewHolder(itemView);
            case IMAGE_TYPE:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.inflatable_lesson_script_image, parent, false);
                return new LessonScriptImageViewHolder(itemView);
            default:
                itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.inflatable_lesson_script_sentence_item, parent, false);
                return new LessonScriptSentenceItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position){
        if (holder instanceof LessonScriptSentenceItemViewHolder){
            ScriptSentence sentence = sentences.get(position);
            int speakerIndex = speakerIndices.get(sentence.getSpeaker());
            int colorAttrID = getSpeakerColorAttrID(speakerIndex);
            ((LessonScriptSentenceItemViewHolder)holder).setIcon(sentence.getSpeakerName().getEnglish(), colorAttrID);
            ((LessonScriptSentenceItemViewHolder) holder).setSentence(sentence.getSentenceEN());
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleExtraInfo(holder.getAdapterPosition());
                }
            });
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    ScriptSentence sentence = sentences.get(holder.getAdapterPosition());
                    TextToSpeechHelper.startTextToSpeech(textToSpeech, sentence.getSentenceEN());
                    //we want to intercept the click event
                    return true;
                }
            });
        } else if (holder instanceof LessonScriptSentenceExtraInfoViewHolder){
            ScriptSentence sentence = sentences.get(sentenceIndexOfCurrentlyShownInfo);
            int speakerIndex = speakerIndices.get(sentence.getSpeaker());
            int colorAttrID = getSpeakerColorAttrID(speakerIndex);
            ((LessonScriptSentenceExtraInfoViewHolder) holder).setBackgroundColor(colorAttrID);
            ((LessonScriptSentenceExtraInfoViewHolder) holder).setTranslation(sentence.getSentenceJP());
            String speaker = sentence.getSpeaker().getName().getJapanese();
            if (ScriptSpeaker.isGuestSpeaker(speaker)){
                speaker = Integer.toString(ScriptSpeaker.getGuestSpeakerNumber(speaker));
            }
            //'~' so we can better differentiate speaker and translation
            ((LessonScriptSentenceExtraInfoViewHolder) holder).setContent("~ " + speaker + " ~");
        } else if (holder instanceof LessonScriptToggleViewHolder){
            ((LessonScriptToggleViewHolder) holder).setLessonToggleButton(lessonToggleEnabled,
                    lessonNumber, totalLessonCt, listener);
            ((LessonScriptToggleViewHolder) holder).setLessonNumber(lessonNumber, totalLessonCt);
        } else if (holder instanceof LessonScriptImageViewHolder){
            ((LessonScriptImageViewHolder) holder).setImage(imageURL);
        }
    }

    private int getSpeakerColorAttrID(int speakerIndex){
        switch (speakerIndex){
            case 1 :
                return R.attr.color300;
            case 2 :
                return R.attr.colorAccent300;
            case 3 :
                return R.attr.color500;
            default :
                return R.attr.colorAccent500;
        }
    }

    private void toggleExtraInfo(int position){
        if (position == sentenceIndexOfCurrentlyShownInfo){
            sentences.remove(position+1);
            notifyItemRemoved(position+1);
            sentenceIndexOfCurrentlyShownInfo = -1;
        } else {
            if (sentenceIndexOfCurrentlyShownInfo != -1){
                sentences.remove(sentenceIndexOfCurrentlyShownInfo+1);
                notifyItemRemoved(sentenceIndexOfCurrentlyShownInfo+1);
                //the position decrements if the removed item is above the position
                if (position > sentenceIndexOfCurrentlyShownInfo)
                    position--;
            }
            ScriptSentence extraInfo = new ScriptSentence();
            extraInfo.setSentence(SENTENCE_INFO_TAG, SENTENCE_INFO_TAG);
            sentences.add(position+1, extraInfo);
            notifyItemInserted(position+1);

            sentenceIndexOfCurrentlyShownInfo = position;
        }
    }

    void isLoading(){
        lessonToggleEnabled = false;
        hideAllButToggle();
    }

    private void hideAllButToggle(){
        if (!sentences.get(0).getSentenceJP().equals(LESSON_NUMBER_TOGGLE_TAG)){
            return;
        }
        sentences = new LinkedList<>(sentences.subList(0,1));
        notifyDataSetChanged();
    }
}
