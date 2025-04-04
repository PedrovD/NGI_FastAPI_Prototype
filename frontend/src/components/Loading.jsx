/**
 * Creates a Loading component
 */
export default function Loading({ size = "1.5rem" }) {
    return (
        <div className="w-full flex justify-center">
            <svg data-testid="loading-svg" width={size} height={size} viewBox="0 0 16 16" fill="currentColor" className="animate-spin" role="img" aria-label="Laden">
                <g fill="currentColor" fillRule="evenodd" clipRule="evenodd">
                    <path d="M8 1.5a6.5 6.5 0 100 13 6.5 6.5 0 000-13zM0 8a8 8 0 1116 0A8 8 0 010 8z" opacity=".2" />
                    <path d="M7.25.75A.75.75 0 018 0a8 8 0 018 8 .75.75 0 01-1.5 0A6.5 6.5 0 008 1.5a.75.75 0 01-.75-.75z" />
                </g>
            </svg>
        </div>
    );
}