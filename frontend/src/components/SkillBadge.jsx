import { useRef } from "react";
import Tooltip from "./Tooltip";

/**
 * @param {{
 * children: React.ReactNode,
 * skillName: string,
 * isPending?: boolean
 * }} props
 * @returns {JSX.Element}
 */
export default function SkillBadge({ children, skillName, isPending, onClick = null, ariaLabel = null }) {
    const toolTipRef = useRef(null);

    let classNames = isPending ? 'bg-gray-300 text-black border border-gray-400' : 'from-primary to-darkPrimary bg-gradient-to-r text-white';
    classNames += ' px-3 py-1 text-nowrap text-sm font-medium rounded-full shadow-md';

    const content = (
        <>
            {skillName}
            {children}
            {isPending && (
                <Tooltip parentRef={toolTipRef}>
                    In afwachting van goedkeuring
                </Tooltip>
            )}
        </>
    );

    if (onClick) {
        return (
            <button ref={toolTipRef} className={classNames} onClick={onClick} aria-label={ariaLabel}>
                {content}
            </button>
        )
    }

    return (
        <span ref={toolTipRef} className={classNames}>
            {content}
        </span>
    )
}