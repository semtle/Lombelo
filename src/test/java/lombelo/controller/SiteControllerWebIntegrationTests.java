package lombelo.controller;
import lombelo.AbstractionWebIntegrationTests;
import lombelo.model.Note;
import lombelo.model.NoteRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.RequestBuilder;

import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

/**
 * @author Niklas Wünsche
 */
public class SiteControllerWebIntegrationTests extends AbstractionWebIntegrationTests {

    @Autowired private NoteRepository notes;

    @Test
    public void executeLandingPageMapper() throws Exception {
        RequestBuilder serviceRequest = post("/");

        mvc.perform(serviceRequest)
                .andExpect(view().name("landingPage"));
    }

    @Test
    public void executeAddNoteMapper() throws Exception {
        RequestBuilder serviceRequest = post("/addNote");

        mvc.perform(serviceRequest)
                .andExpect(model().attributeExists("content"))
                .andExpect(view().name("addNote"));
    }

    @Test
    public void executeSaveNewNote() throws Exception {
        String noteTitle = "My Note";
        String textTitle = "BlaBlaBla";
        RequestBuilder serviceRequest = post("/addNote/created")
                .param("titleOfNote", noteTitle)
                .param("textOfNote", textTitle);

        mvc.perform(serviceRequest)
                .andExpect(view().name("landingPage"));

        Optional<Note> savedNote = StreamSupport
                .stream(notes.findAll().spliterator(), false)
                .filter(note -> note.getTitle().equals(noteTitle))
                .findAny();

        assertThat(savedNote.isPresent(), is(true));
        assertThat(savedNote.get().getText(), is(textTitle));
    }

    @Test
    public void executeShowAllNotes() throws Exception {
        RequestBuilder serviceRequest = post("/showNotes");
        mvc.perform(serviceRequest)
            .andExpect(model().attribute("notes", notes.findAll()))
            .andExpect(view().name("showNotes"));
    }

    @Test
    public void executeEditNote() throws Exception {
        Note note = new Note("title", "text");
        notes.save(note);

        RequestBuilder serviceRequest = get("/editNote/" + note.getId());

        mvc.perform(serviceRequest)
                .andExpect(view().name("editNote"));
    }

    @Test
    public void executeFinishEditNote() throws Exception {
        Note note = new Note("title", "text");
        notes.save(note);

        RequestBuilder serviceRequest = post("/editNote/finished/" + note.getId())
                .param("titleOfNote", "newTitle")
                .param("textOfNote", "newText");

        mvc.perform(serviceRequest)
                .andExpect(status().is3xxRedirection());

        Optional<Note> editedNote = StreamSupport
                .stream(notes.findAll().spliterator(), false)
                .filter(savedNote -> savedNote.getTitle().equals("newTitle"))
                .findAny();

        assertThat(editedNote.isPresent(), is(true));
        assertThat(editedNote.get().getText(), is("newText"));
    }

    @Test
    public void executeRemoveNote() throws Exception {
        Note toRemove = new Note("remove", "remove");
        notes.save(toRemove);

        RequestBuilder serviceRequest = post("/removeNote/" + toRemove.getId());

        mvc.perform(serviceRequest)
                .andExpect(status().is3xxRedirection());

        Optional<Note> editedNote = StreamSupport
                .stream(notes.findAll().spliterator(), false)
                .filter(savedNote -> savedNote.getTitle().equals("remove"))
                .findAny();

        assertThat(editedNote.isPresent(), is(false));
    }

}
