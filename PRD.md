# Product Requirements Document

## 1. Product / Feature Name
HIStudy Backend Service

## 2. Problem Statement
Handong students and administrators currently face slow, inconsistent manual coordination for semester study groups. Students need fair matching based on friend requests and course preferences, while admins need clear visibility into participation and outcomes.

## 3. Purpose and Value
HIStudy should reduce coordination overhead and make study-group operations predictable each academic term. It provides value by improving matching fairness, increasing study activity continuity, and giving admins transparent operational control and reporting visibility.

## 4. Goals
- Provide reliable term-based study application, matching, and group operations for students, members, and admins.
- Deliver transparent activity tracking through report and public metrics flows.
- Maintain role-safe access and privacy-aware data exposure, including v2 masking behavior.

## 5. Non-Goals
- Real-time chat, video meeting, or scheduling assistant functionality.
- LMS gradebook synchronization.
- Payments, points, or financial incentives.
- Broad multi-tenant operation beyond current institutional context.

## 6. Target Users / Personas
- Student (`USER`): submits study preferences, searches peers/courses, views own status.
- Study Member (`MEMBER`): manages study-group reports and images.
- Admin (`ADMIN`): manages terms, users, matching, and operational monitoring.
- Public Viewer: checks aggregate activity and ranked group summaries.

## 7. Expected Behavior
### 7.1 Core User Journeys
1. A student signs up/logs in, submits term-scoped study preferences, and can re-submit while still unassigned.
2. An admin triggers matching for the current term and reviews grouped/unmatched status.
3. A study member uploads images and creates/updates/deletes reports for their own group.
4. A public viewer accesses aggregate activity and ranking endpoints without sensitive personal details.

### 7.2 Functional Expectations
- Exactly one academic term is current at a time; core operations are scoped to that current term.
- Protected APIs require valid bearer authentication unless explicitly public.
- Role permissions are enforced: `USER`, `MEMBER`, and `ADMIN` capabilities are separated.
- Matching runs in two phases: friend-first (minimum group size 2), then course-first (target group size 3-5, with insufficient bucket leftovers unmatched).
- Group tags are unique per term and continue from the latest existing tag.
- Group courses are derived from member preferences, prioritizing courses preferred by at least two members when possible.
- Report operations are member-restricted and support optional images via upload path reuse behavior.
- v2 user/member search and team views expose masked student identifiers.

### 7.3 Success Signals
- Increased application completion rate among active students each term.
- Matching coverage improves and remains stable term-over-term.
- Matching execution is operationally fast enough for admin workflows.
- Active groups and total reported study time trend upward.
- Report submission frequency per active group remains consistent.

## 8. Constraints
- The product must remain term-scoped and institution-context oriented.
- Role-based authorization must be consistently enforced across all protected endpoints.
- Public endpoints must avoid exposing personal identifiable details.
- Compatibility should be maintained for existing deprecated endpoints while preferring v2 behavior.

## 9. Assumptions
- Handong academic-term lifecycle remains the organizing model for operations.
- Core user roles (`USER`, `MEMBER`, `ADMIN`) remain unchanged.
- Current API surface categories remain stable: public, auth, users/forms, courses, team/reports, admin.
- Study activity evidence continues to rely on report submissions and optional image attachments.