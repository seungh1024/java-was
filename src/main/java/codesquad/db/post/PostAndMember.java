package codesquad.db.post;

public record PostAndMember(
        long id,
        String title,
        String content,
        long memberPk,
        String memberName
){
}
