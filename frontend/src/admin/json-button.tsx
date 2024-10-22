
export const JsonButton = ({surveyId}: {surveyId: string}) => (
    <a href={`${import.meta.env.VITE_ZTOR_URL}/company-surveys/${surveyId}`} className="p-button" css={{
        textDecoration: 'none',
        whiteSpace: 'nowrap',
    }}>
        {"{}"}
    </a>
)