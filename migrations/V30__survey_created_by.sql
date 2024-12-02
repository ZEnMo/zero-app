ALTER TABLE company_survey
    ADD created_by_id uuid
        CONSTRAINT fk_company_survey_created_by_id__id
            REFERENCES "user"
            ON UPDATE RESTRICT ON DELETE RESTRICT;
