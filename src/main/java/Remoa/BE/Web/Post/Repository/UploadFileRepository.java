package Remoa.BE.Web.Post.Repository;

import Remoa.BE.Web.Post.Domain.Post;
import Remoa.BE.Web.Post.Domain.UploadFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class UploadFileRepository {

    private final EntityManager em;


    public Optional<UploadFile> findById(Long fileId){
        return Optional.ofNullable(em.find(UploadFile.class, fileId));
    }

    public void saveFile(UploadFile file) {
        em.persist(file);
    }

    public void modifyFile(UploadFile file) {
        em.merge(file);
    }

    public List<UploadFile> findFilesByPost(Post post) {
        return new ArrayList<>(em.createQuery("select uf from UploadFile uf where uf.post = :post", UploadFile.class)
                .setParameter("post", post)
                .getResultList());
    }

    public void deleteById(UploadFile file) {
        em.createQuery("delete from UploadFile u where u.uploadFileId = :id")
                .setParameter("id", file.getUploadFileId())
                .executeUpdate();
    }

    public void deleteByPost(Post post) {
        em.createQuery("delete from UploadFile u where u.post = :post")
                .setParameter("post", post)
                .executeUpdate();
    }

}
